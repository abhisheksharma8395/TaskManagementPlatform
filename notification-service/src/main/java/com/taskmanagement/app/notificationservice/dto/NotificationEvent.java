package com.taskmanagement.app.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long recipientId;
    private String recipientEmail;
    private Long actorId;
    private String type;         // ASSIGNMENT, MENTION, DUE_DATE, COMMENT, MOVE, BROADCAST
    private String title;
    private String message;
    private Long relatedId;
    private String relatedType;  // CARD or BOARD
    private String deepLinkUrl;
}
