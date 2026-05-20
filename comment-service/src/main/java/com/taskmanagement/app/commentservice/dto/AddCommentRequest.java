package com.taskmanagement.app.commentservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class AddCommentRequest {
    @NotNull(message = "cardId is required") private Long cardId;
    @NotBlank(message = "Content is required") @Size(max = 2000) private String content;
    private Long parentCommentId;  // null = top-level comment
}
