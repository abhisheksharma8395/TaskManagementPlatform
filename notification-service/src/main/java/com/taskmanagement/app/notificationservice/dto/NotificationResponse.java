package com.taskmanagement.app.notificationservice.dto;
import lombok.Data; import java.time.LocalDateTime;
@Data
public class NotificationResponse {
    private Long notificationId;
    private Long recipientId;
    private Long actorId;
    private String type;
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;
    private String deepLinkUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
}
