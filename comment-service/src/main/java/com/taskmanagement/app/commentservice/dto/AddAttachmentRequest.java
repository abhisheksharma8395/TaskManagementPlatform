package com.taskmanagement.app.commentservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data public class AddAttachmentRequest {
    private Long cardId;
    private String fileName;
    private MultipartFile file;
    private String fileUrl;
    private String viewerUrl;
    private String fileType;
    private Long sizeKb;
}
