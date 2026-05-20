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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private CardServiceClient cardServiceClient;
    @Mock
    private S3Service s3Service;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment sampleComment;
    private CardResponse sampleCard;
    private final Long CARD_ID = 5L;
    private final Long COMMENT_ID = 1L;
    private final Long AUTHOR_ID = 10L;
    private final Long OTHER_ID = 20L;
    private final String TOKEN = "Bearer test.token";

    @BeforeEach
    void setUp() {
        sampleCard = new CardResponse();
        sampleCard.setCardId(CARD_ID);
        sampleCard.setTitle("Test Card");
        sampleCard.setArchived(false);
        sampleCard.setAssigneeId(OTHER_ID);

        sampleComment = new Comment();
        sampleComment.setCommentId(COMMENT_ID);
        sampleComment.setCardId(CARD_ID);
        sampleComment.setAuthorId(AUTHOR_ID);
        sampleComment.setContent("Test comment");
        sampleComment.setDeleted(false);
    }

    // ─── addComment ───────────────────────────────────────────────────────────

    @Test
    void addComment_happyPath_returnsResponse() {
        AddCommentRequest req = new AddCommentRequest();
        req.setCardId(CARD_ID);
        req.setContent("This is a comment");

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

        CommentResponse result = commentService.addComment(req, AUTHOR_ID, TOKEN);

        assertThat(result).isNotNull();
        assertThat(result.getCommentId()).isEqualTo(COMMENT_ID);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_archivedCard_throwsBadRequest() {
        sampleCard.setArchived(true);
        AddCommentRequest req = new AddCommentRequest();
        req.setCardId(CARD_ID);
        req.setContent("comment");

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));

        assertThatThrownBy(() -> commentService.addComment(req, AUTHOR_ID, TOKEN))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("archived");
    }

    @Test
    void addComment_reply_parentNotFound_throwsResourceNotFound() {
        AddCommentRequest req = new AddCommentRequest();
        req.setCardId(CARD_ID);
        req.setContent("reply");
        req.setParentCommentId(999L);

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(req, AUTHOR_ID, TOKEN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parent comment not found");
    }

    @Test
    void addComment_replyToReply_throwsBadRequest() {
        Comment parentComment = new Comment();
        parentComment.setCommentId(2L);
        parentComment.setCardId(CARD_ID);
        parentComment.setParentCommentId(1L); // this is already a reply

        AddCommentRequest req = new AddCommentRequest();
        req.setCardId(CARD_ID);
        req.setContent("nested reply");
        req.setParentCommentId(2L);

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentComment));

        assertThatThrownBy(() -> commentService.addComment(req, AUTHOR_ID, TOKEN))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot reply to a reply");
    }

    @Test
    void addComment_replyToDeletedComment_throwsBadRequest() {
        Comment deletedParent = new Comment();
        deletedParent.setCommentId(2L);
        deletedParent.setCardId(CARD_ID);
        deletedParent.setDeleted(true);

        AddCommentRequest req = new AddCommentRequest();
        req.setCardId(CARD_ID);
        req.setContent("reply to deleted");
        req.setParentCommentId(2L);

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(deletedParent));

        assertThatThrownBy(() -> commentService.addComment(req, AUTHOR_ID, TOKEN))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deleted comment");
    }

    // ─── getCommentsByCard ────────────────────────────────────────────────────

    @Test
    void getCommentsByCard_returnsList() {
        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(commentRepository.findByCardIdAndIsDeletedFalseAndParentCommentIdIsNullOrderByCreatedAtAsc(CARD_ID))
                .thenReturn(List.of(sampleComment));
        when(commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(COMMENT_ID))
                .thenReturn(List.of());

        List<CommentResponse> results = commentService.getCommentsByCard(CARD_ID, TOKEN);

        assertThat(results).hasSize(1);
    }

    // ─── getCommentById ───────────────────────────────────────────────────────

    @Test
    void getCommentById_found_returnsResponse() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));
        when(commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(COMMENT_ID))
                .thenReturn(List.of());

        CommentResponse result = commentService.getCommentById(COMMENT_ID);

        assertThat(result.getCommentId()).isEqualTo(COMMENT_ID);
    }

    @Test
    void getCommentById_notFound_throwsResourceNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getReplies ───────────────────────────────────────────────────────────

    @Test
    void getReplies_returnsList() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));
        when(commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(COMMENT_ID))
                .thenReturn(List.of());

        List<CommentResponse> results = commentService.getReplies(COMMENT_ID);

        assertThat(results).isEmpty();
    }

    // ─── updateComment ────────────────────────────────────────────────────────

    @Test
    void updateComment_owner_succeeds() {
        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Updated content");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

        CommentResponse result = commentService.updateComment(COMMENT_ID, req, AUTHOR_ID);

        assertThat(result).isNotNull();
        verify(commentRepository).save(sampleComment);
    }

    @Test
    void updateComment_deletedComment_throwsBadRequest() {
        sampleComment.setDeleted(true);
        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Update attempt");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));

        assertThatThrownBy(() -> commentService.updateComment(COMMENT_ID, req, AUTHOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deleted");
    }

    @Test
    void updateComment_nonAuthor_throwsAccessDenied() {
        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Unauthorized update");

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));

        assertThatThrownBy(() -> commentService.updateComment(COMMENT_ID, req, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own comments");
    }

    // ─── deleteComment ────────────────────────────────────────────────────────

    @Test
    void deleteComment_author_softDeletes() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

        commentService.deleteComment(COMMENT_ID, AUTHOR_ID);

        verify(commentRepository).save(sampleComment);
    }

    @Test
    void deleteComment_nonAuthor_throwsAccessDenied() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));

        assertThatThrownBy(() -> commentService.deleteComment(COMMENT_ID, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteComment_alreadyDeleted_throwsBadRequest() {
        sampleComment.setDeleted(true);
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(sampleComment));

        assertThatThrownBy(() -> commentService.deleteComment(COMMENT_ID, AUTHOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already deleted");
    }

    // ─── getCommentCount ──────────────────────────────────────────────────────

    @Test
    void getCommentCount_returnsCount() {
        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(commentRepository.countByCardIdAndIsDeletedFalse(CARD_ID)).thenReturn(5L);

        long count = commentService.getCommentCount(CARD_ID, TOKEN);

        assertThat(count).isEqualTo(5L);
    }

    // ─── getAttachmentsByCard ─────────────────────────────────────────────────

    @Test
    void getAttachmentsByCard_returnsList() {
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(1L);
        attachment.setCardId(CARD_ID);
        attachment.setUploaderId(AUTHOR_ID);
        attachment.setFileName("test.pdf");
        attachment.setFileKey("key/test.pdf");

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(sampleCard));
        when(attachmentRepository.findByCardId(CARD_ID)).thenReturn(List.of(attachment));
        when(s3Service.generatePresignedUrl("key/test.pdf")).thenReturn("https://cdn.example.com/test.pdf");

        List<AttachmentResponse> results = commentService.getAttachmentsByCard(CARD_ID, TOKEN);

        assertThat(results).hasSize(1);
    }

    // ─── deleteAttachment ─────────────────────────────────────────────────────

    @Test
    void deleteAttachment_owner_succeeds() {
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(1L);
        attachment.setCardId(CARD_ID);
        attachment.setUploaderId(AUTHOR_ID);
        attachment.setFileKey("key/test.pdf");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        commentService.deleteAttachment(1L, AUTHOR_ID);

        verify(s3Service).deleteFile("key/test.pdf");
        verify(attachmentRepository).deleteById(1L);
    }

    @Test
    void deleteAttachment_nonOwner_throwsAccessDenied() {
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(1L);
        attachment.setUploaderId(AUTHOR_ID);

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        assertThatThrownBy(() -> commentService.deleteAttachment(1L, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own");
    }
}
