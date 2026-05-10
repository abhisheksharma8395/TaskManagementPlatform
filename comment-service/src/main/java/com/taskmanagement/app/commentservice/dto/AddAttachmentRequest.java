package com.taskmanagement.app.commentservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data public class AddAttachmentRequest {
    @NotNull
    private Long cardId;

    @NotNull
    private MultipartFile file;
}
