package com.taskmanagement.app.commentservice.service;

import com.taskmanagement.app.commentservice.dto.*;
import java.util.List;

public interface CommentService {
    CommentResponse addComment(AddCommentRequest request, Long authorId , String token);
    List<CommentResponse> getCommentsByCard(Long cardId , String token);
    CommentResponse getCommentById(Long commentId);
    List<CommentResponse> getReplies(Long parentCommentId);
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long requesterId);
    void deleteComment(Long commentId, Long requesterId);
    long getCommentCount(Long cardId , String token);
    AttachmentResponse addAttachment(AddAttachmentRequest request, Long uploaderId , String token);
    List<AttachmentResponse> getAttachmentsByCard(Long cardId , String token);
    void deleteAttachment(Long attachmentId, Long requesterId);
}
