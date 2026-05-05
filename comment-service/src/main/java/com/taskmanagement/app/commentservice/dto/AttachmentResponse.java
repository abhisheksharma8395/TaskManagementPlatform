package com.taskmanagement.app.commentservice.dto;
import lombok.Data; import java.time.LocalDateTime;
@Data public class AttachmentResponse {
    private Long attachmentId;
    private Long cardId;
    private Long uploaderId;
    private String fileName;
    private String fileUrl;
    private String viewerUrl;
    private String fileType;
    private Long sizeKb;
    private LocalDateTime uploadedAt;
}
