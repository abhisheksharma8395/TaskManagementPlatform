package com.taskmanagement.app.commentservice.dto;
import lombok.Data; import java.time.LocalDateTime; import java.util.List;
@Data public class CommentResponse {
    private Long commentId;
    private Long cardId;
    private Long authorId;
    private String content;
    private Long parentCommentId;
    private boolean isDeleted;
    private int replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies;
}
