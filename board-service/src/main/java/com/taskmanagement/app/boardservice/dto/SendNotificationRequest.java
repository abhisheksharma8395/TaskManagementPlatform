package com.taskmanagement.app.boardservice.dto;
import lombok.Data;
@Data
public class SendNotificationRequest {
    private Long recipientId;
    private String recipientEmail;
    private Long actorId;
    private String type;       // ASSIGNMENT, BROADCAST
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType; // CARD or BOARD
    private String deepLinkUrl;
}