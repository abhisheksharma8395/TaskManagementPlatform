package com.taskmanagement.app.commentservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class AddAttachmentRequest {
    @NotNull private Long cardId;
    @NotBlank private String fileName;
    @NotBlank private String fileUrl;
    private String fileType;
    @NotNull(message = "File size required")
    @Max(value = 10240, message = "File size can't be more than 10MB") // 10MB = 10240 KB
    private Long sizeKb;
}
