package com.taskmanagement.app.commentservice.dto;

import lombok.Data;

@Data
public class SendNotificationRequest {
    private Long recipientId;
    private String recipientEmail;
    private Long actorId;
    private String type;       // COMMENT or MENTION
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType; // CARD or BOARD
    private String deepLinkUrl;
}
