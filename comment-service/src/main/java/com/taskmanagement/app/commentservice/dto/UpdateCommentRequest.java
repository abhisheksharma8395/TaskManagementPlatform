package com.taskmanagement.app.commentservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class UpdateCommentRequest {
    @NotBlank(message = "Content is required") @Size(max = 2000) private String content;
}
