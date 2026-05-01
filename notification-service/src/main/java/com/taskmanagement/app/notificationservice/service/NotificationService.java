package com.taskmanagement.app.notificationservice.service;

import com.taskmanagement.app.notificationservice.dto.*;
import java.util.List;

public interface NotificationService {

    NotificationResponse send(SendNotificationRequest request);

    List<NotificationResponse> sendBulk(SendBulkNotificationRequest request);

    void sendEmail(String toEmail, String subject, String body);

    List<NotificationResponse> getByRecipient(Long recipientId);

    List<NotificationResponse> getUnreadByRecipient(Long recipientId);

    NotificationResponse markAsRead(Long notificationId, Long recipientId);

    void markAllRead(Long recipientId);

    void deleteRead(Long recipientId);

    void deleteNotification(Long notificationId, Long recipientId);

    UnreadCountResponse getUnreadCount(Long recipientId);

    List<NotificationResponse> getAll();
}
