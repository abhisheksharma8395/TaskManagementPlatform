package com.taskmanagement.app.notificationservice.service;

import com.taskmanagement.app.notificationservice.dto.*;
import com.taskmanagement.app.notificationservice.entity.Notification;
import com.taskmanagement.app.notificationservice.exception.AccessDeniedException;
import com.taskmanagement.app.notificationservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Read the sender address from application.properties
    @Value("${spring.mail.username:noreply@flowboard.com}")
    private String fromEmail;

    @Override
    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        Notification notification = buildNotification(
                request.getRecipientId(), request.getActorId(), request.getType(),
                request.getTitle(), request.getMessage(),
                request.getRelatedId(), request.getRelatedType(), request.getDeepLinkUrl());

        Notification saved = notificationRepository.save(notification);

        // Send email for critical event types only, if recipientEmail is provided
        if (request.getRecipientEmail() != null && isCriticalType(request.getType())) {
            sendEmail(request.getRecipientEmail(), request.getTitle(), request.getMessage());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<NotificationResponse> sendBulk(SendBulkNotificationRequest request) {
        List<Long> recipients = request.getRecipientIds();

        if (recipients == null || recipients.isEmpty()) {
            return List.of();
        }

        List<NotificationResponse> responses = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++) {
            Long recipientId = recipients.get(i);
            Notification n = buildNotification(
                    recipientId, null, "BROADCAST",
                    request.getTitle(), request.getMessage(),
                    null, null, request.getDeepLinkUrl());
            responses.add(toResponse(notificationRepository.save(n)));

            // Send email if recipient emails are provided and match by index
            if (request.getRecipientEmails() != null
                    && i < request.getRecipientEmails().size()
                    && request.getRecipientEmails().get(i) != null) {
                sendEmail(request.getRecipientEmails().get(i),
                        request.getTitle(), request.getMessage());
            }
        }
        return responses;
    }


    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        if (mailSender == null) {
            System.out.println("[NotificationService] Mail sender not configured. Skipping email to: " + toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[FlowBoard] " + subject);
            message.setText(body + "\n\n— The FlowBoard Team");
            mailSender.send(message);
            System.out.println("[NotificationService] Email sent to: " + toEmail);
        } catch (Exception e) {
            // Log and continue — never let email failure break the main flow
            System.err.println("[NotificationService] Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }


    @Override
    public List<NotificationResponse> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long recipientId) {
        Notification n = findOrThrow(notificationId);
        assertOwns(n, recipientId);
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Override
    @Transactional
    public void markAllRead(Long recipientId) {
        notificationRepository.markAllReadByRecipient(recipientId);
    }

    @Override
    @Transactional
    public void deleteRead(Long recipientId) {
        notificationRepository.deleteReadByRecipient(recipientId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long recipientId) {
        Notification n = findOrThrow(notificationId);
        assertOwns(n, recipientId);
        notificationRepository.delete(n);
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long recipientId) {
        UnreadCountResponse r = new UnreadCountResponse();
        r.setRecipientId(recipientId);
        r.setUnreadCount(notificationRepository.countByRecipientIdAndIsReadFalse(recipientId));
        return r;
    }

    /**
     * Per case study: email only for ASSIGNMENT and DUE_DATE (critical events).
     */
    private boolean isCriticalType(String type) {
        return "ASSIGNMENT".equals(type) || "DUE_DATE".equals(type);
    }

    private Notification buildNotification(Long recipientId, Long actorId, String type,
                                           String title, String message,
                                           Long relatedId, String relatedType, String deepLinkUrl) {
        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setActorId(actorId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        n.setRelatedType(relatedType);
        n.setDeepLinkUrl(deepLinkUrl);
        return n;
    }

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }

    private void assertOwns(Notification n, Long recipientId) {
        if (!n.getRecipientId().equals(recipientId))
            throw new AccessDeniedException("You can only manage your own notifications");
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setNotificationId(n.getNotificationId());
        r.setRecipientId(n.getRecipientId());
        r.setActorId(n.getActorId());
        r.setType(n.getType());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setRelatedId(n.getRelatedId());
        r.setRelatedType(n.getRelatedType());
        r.setDeepLinkUrl(n.getDeepLinkUrl());
        r.setRead(n.isRead());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}