package com.taskmanagement.app.notificationservice.dto;
import lombok.Data;
@Data
public class UnreadCountResponse {
    private Long recipientId;
    private long unreadCount;
}
