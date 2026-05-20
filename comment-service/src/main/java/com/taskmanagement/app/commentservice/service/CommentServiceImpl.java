package com.taskmanagement.app.commentservice.service;

import com.taskmanagement.app.commentservice.dto.*;
import com.taskmanagement.app.commentservice.entity.Attachment;
import com.taskmanagement.app.commentservice.entity.Comment;
import com.taskmanagement.app.commentservice.exception.AccessDeniedException;
import com.taskmanagement.app.commentservice.exception.BadRequestException;
import com.taskmanagement.app.commentservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.commentservice.feign.CardServiceClient;
import com.taskmanagement.app.commentservice.messaging.NotificationPublisher;
import com.taskmanagement.app.commentservice.repository.AttachmentRepository;
import com.taskmanagement.app.commentservice.repository.CommentRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentRepository commentRepository;
    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private CardServiceClient cardServiceClient;
    @Autowired private NotificationPublisher notificationPublisher;
    @Autowired private S3Service s3Service;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf",
            "application/msword"
    );

    @Override
    @Transactional
    public CommentResponse addComment(AddCommentRequest request, Long authorId, String token) {

        CardResponse card = fetchCardOrThrow(request.getCardId(), token);
        if (card.isArchived()) {
            throw new BadRequestException("Cannot add a comment to archived card: " + request.getCardId());
        }
        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found: " + request.getParentCommentId()));
            if (parent.getParentCommentId() != null)
                throw new BadRequestException("Cannot reply to a reply — only one level of threading is supported");
            if (!parent.getCardId().equals(request.getCardId()))
                throw new BadRequestException("Parent comment does not belong to card: " + request.getCardId());
            if (parent.isDeleted())
                throw new BadRequestException("Cannot reply to a deleted comment");
        }

        Comment comment = new Comment();
        comment.setCardId(request.getCardId());
        comment.setAuthorId(authorId);
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        Comment saved = commentRepository.save(comment);

        // Publish notification to RabbitMQ — notify the card assignee
        if (card.getAssigneeId() != null && !card.getAssigneeId().equals(authorId)) {
            boolean isMention = request.getContent() != null
                    && request.getContent().matches(".*@\\S+.*");

            String truncatedContent = request.getContent() != null && request.getContent().length() > 100
                    ? request.getContent().substring(0, 100) + "..."
                    : request.getContent();

            NotificationEvent event = new NotificationEvent();
            event.setRecipientId(card.getAssigneeId());
            event.setRecipientEmail(null); // COMMENT/MENTION — in-app only, no email
            event.setActorId(authorId);
            event.setType(isMention ? "MENTION" : "COMMENT");
            event.setTitle(isMention ? "You were mentioned in a comment" : "New comment on your card");
            event.setMessage(truncatedContent);
            event.setRelatedId(request.getCardId());
            event.setRelatedType("CARD");
            event.setDeepLinkUrl("/cards/" + request.getCardId());
            notificationPublisher.publish(event);
        }

        return toResponse(saved, false);
    }

    @Override
    public List<CommentResponse> getCommentsByCard(Long cardId, String token) {
        fetchCardOrThrow(cardId, token);
        return commentRepository
                .findByCardIdAndIsDeletedFalseAndParentCommentIdIsNullOrderByCreatedAtAsc(cardId)
                .stream()
                .map(c -> toResponse(c, true))
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return toResponse(findCommentOrThrow(commentId), true);
    }

    @Override
    public List<CommentResponse> getReplies(Long parentCommentId) {
        findCommentOrThrow(parentCommentId);
        return commentRepository
                .findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(parentCommentId)
                .stream()
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long requesterId) {
        Comment comment = findCommentOrThrow(commentId);
        if (comment.isDeleted()) throw new BadRequestException("Cannot edit a deleted comment");
        if (!comment.getAuthorId().equals(requesterId))
            throw new AccessDeniedException("You can only edit your own comments");
        comment.setContent(request.getContent());
        return toResponse(commentRepository.save(comment), false);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long requesterId) {
        Comment comment = findCommentOrThrow(commentId);
        if (!comment.getAuthorId().equals(requesterId))
            throw new AccessDeniedException("You can only delete your own comments");
        if (comment.isDeleted()) throw new BadRequestException("Comment is already deleted");
        comment.setDeleted(true);
        comment.setContent("[deleted]");
        commentRepository.save(comment);
    }

    @Override
    public long getCommentCount(Long cardId, String token) {
        fetchCardOrThrow(cardId, token);
        return commentRepository.countByCardIdAndIsDeletedFalse(cardId);
    }

    @Override
    @Transactional
    public AttachmentResponse addAttachment(AddAttachmentRequest request, Long uploaderId, String token) throws IOException {
        CardResponse card = fetchCardOrThrow(request.getCardId(), token);
        if (card.isArchived())
            throw new BadRequestException("Cannot add an attachment to archived card: " + request.getCardId());

        MultipartFile file = request.getFile();
        String fileType = file.getContentType();
        if (!ALLOWED_TYPES.contains(fileType))
            throw new BadRequestException(fileType + " File Type is not allowed");

        long sizeKb = file.getSize() / 1024;
        if (sizeKb > 10240)
            throw new BadRequestException("File size can't be more than 10 mb. Your current file size is: " + sizeKb / 1024 + " mb");

        String fileKey = s3Service.uploadFile(file);

        boolean duplicate = attachmentRepository.findByCardId(request.getCardId())
                .stream()
                .anyMatch(a -> a.getFileName().equals(file.getOriginalFilename()));
        if (duplicate) {
            s3Service.deleteFile(fileKey);
            throw new BadRequestException("This file is already attached to card: " + request.getCardId());
        }

        Attachment attachment = new Attachment();
        attachment.setCardId(request.getCardId());
        attachment.setUploaderId(uploaderId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileKey(fileKey);
        attachment.setFileType(fileType);
        attachment.setSizeKb(sizeKb);

        return toAttachmentResponse(attachmentRepository.save(attachment));
    }

    @Override
    public List<AttachmentResponse> getAttachmentsByCard(Long cardId, String token) {
        fetchCardOrThrow(cardId, token);
        return attachmentRepository.findByCardId(cardId)
                .stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, Long requesterId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));
        if (!attachment.getUploaderId().equals(requesterId))
            throw new AccessDeniedException("You can only delete your own attachments");
        s3Service.deleteFile(attachment.getFileKey());
        attachmentRepository.deleteById(attachmentId);
    }

    private CardResponse fetchCardOrThrow(Long cardId, String token) {
        try {
            CardResponse card = cardServiceClient.getCardById(cardId, token).getBody();
            if (card == null) throw new ResourceNotFoundException("Card not found: " + cardId);
            return card;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Card not found: " + cardId);
        } catch (FeignException e) {
            throw new BadRequestException("Could not verify card " + cardId + " — card-service error: " + e.status());
        }
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private CommentResponse toResponse(Comment c, boolean loadReplies) {
        CommentResponse r = new CommentResponse();
        r.setCommentId(c.getCommentId());
        r.setCardId(c.getCardId());
        r.setAuthorId(c.getAuthorId());
        r.setContent(c.getContent());
        r.setParentCommentId(c.getParentCommentId());
        r.setDeleted(c.isDeleted());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());

        if (loadReplies && c.getParentCommentId() == null) {
            List<CommentResponse> replies = commentRepository
                    .findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(c.getCommentId())
                    .stream()
                    .map(reply -> toResponse(reply, false))
                    .collect(Collectors.toList());
            r.setReplies(replies);
            r.setReplyCount(replies.size());
        } else {
            r.setReplies(Collections.emptyList());
            r.setReplyCount(0);
        }
        return r;
    }

    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.setAttachmentId(attachment.getAttachmentId());
        response.setCardId(attachment.getCardId());
        response.setUploaderId(attachment.getUploaderId());
        response.setFileName(attachment.getFileName());
        response.setFileType(attachment.getFileType());
        response.setSizeKb(attachment.getSizeKb());
        response.setUploadedAt(attachment.getUploadedAt());
        response.setFileUrl(s3Service.generatePresignedUrl(attachment.getFileKey()));
        return response;
    }
}