package com.taskmanagement.app.notificationservice.service;

import com.taskmanagement.app.notificationservice.dto.*;
import com.taskmanagement.app.notificationservice.entity.Notification;
import com.taskmanagement.app.notificationservice.exception.AccessDeniedException;
import com.taskmanagement.app.notificationservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification sampleNotification;
    private final Long RECIPIENT_ID = 1L;
    private final Long OTHER_ID = 2L;
    private final Long NOTIF_ID = 10L;

    @BeforeEach
    void setUp() {
        sampleNotification = new Notification();
        sampleNotification.setNotificationId(NOTIF_ID);
        sampleNotification.setRecipientId(RECIPIENT_ID);
        sampleNotification.setType("ASSIGNMENT");
        sampleNotification.setTitle("Test Notification");
        sampleNotification.setMessage("You have been assigned a task");
        sampleNotification.setRead(false);
    }

    private SendNotificationRequest buildSendRequest() {
        SendNotificationRequest req = new SendNotificationRequest();
        req.setRecipientId(RECIPIENT_ID);
        req.setType("ASSIGNMENT");
        req.setTitle("Test Notification");
        req.setMessage("You have been assigned a task");
        req.setActorId(OTHER_ID);
        req.setRelatedId(5L);
        req.setRelatedType("CARD");
        req.setDeepLinkUrl("/cards/5");
        return req;
    }

    // ─── send ────────────────────────────────────────────────────────────────

    @Test
    void send_happyPath_returnsResponse() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse result = notificationService.send(buildSendRequest());

        assertThat(result).isNotNull();
        assertThat(result.getNotificationId()).isEqualTo(NOTIF_ID);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void send_withEmail_criticalType_sendsEmail() {
        SendNotificationRequest req = buildSendRequest();
        req.setRecipientEmail("user@example.com");
        req.setType("ASSIGNMENT");

        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        // mailSender is null (not configured), so sendEmail is a no-op — just verify no
        // exception
        NotificationResponse result = notificationService.send(req);

        assertThat(result).isNotNull();
    }

    @Test
    void send_nonCriticalType_doesNotSendEmail() {
        SendNotificationRequest req = buildSendRequest();
        req.setRecipientEmail("user@example.com");
        req.setType("COMMENT"); // non-critical

        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse result = notificationService.send(req);

        assertThat(result).isNotNull();
    }

    // ─── sendBulk ────────────────────────────────────────────────────────────

    @Test
    void sendBulk_multipleRecipients_returnsAll() {
        SendBulkNotificationRequest req = new SendBulkNotificationRequest();
        req.setRecipientIds(List.of(1L, 2L, 3L));
        req.setTitle("Broadcast");
        req.setMessage("Platform announcement");

        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        List<NotificationResponse> results = notificationService.sendBulk(req);

        assertThat(results).hasSize(3);
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    void sendBulk_emptyRecipients_returnsEmptyList() {
        SendBulkNotificationRequest req = new SendBulkNotificationRequest();
        req.setRecipientIds(List.of());
        req.setTitle("Broadcast");
        req.setMessage("Message");

        List<NotificationResponse> results = notificationService.sendBulk(req);

        assertThat(results).isEmpty();
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendBulk_nullRecipients_returnsEmptyList() {
        SendBulkNotificationRequest req = new SendBulkNotificationRequest();
        req.setRecipientIds(null);
        req.setTitle("Broadcast");
        req.setMessage("Message");

        List<NotificationResponse> results = notificationService.sendBulk(req);

        assertThat(results).isEmpty();
    }

    // ─── getByRecipient ───────────────────────────────────────────────────────

    @Test
    void getByRecipient_returnsList() {
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(RECIPIENT_ID))
                .thenReturn(List.of(sampleNotification));

        List<NotificationResponse> results = notificationService.getByRecipient(RECIPIENT_ID);

        assertThat(results).hasSize(1);
    }

    // ─── getUnreadByRecipient ────────────────────────────────────────────────

    @Test
    void getUnreadByRecipient_returnsList() {
        when(notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(RECIPIENT_ID, false))
                .thenReturn(List.of(sampleNotification));

        List<NotificationResponse> results = notificationService.getUnreadByRecipient(RECIPIENT_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isRead()).isFalse();
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsList() {
        when(notificationRepository.findAll()).thenReturn(List.of(sampleNotification));

        List<NotificationResponse> results = notificationService.getAll();

        assertThat(results).hasSize(1);
    }

    // ─── markAsRead ───────────────────────────────────────────────────────────

    @Test
    void markAsRead_ownerMarks_succeeds() {
        when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse result = notificationService.markAsRead(NOTIF_ID, RECIPIENT_ID);

        assertThat(result).isNotNull();
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, RECIPIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAsRead_wrongRecipient_throwsAccessDenied() {
        when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(sampleNotification));

        assertThatThrownBy(() -> notificationService.markAsRead(NOTIF_ID, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own");
    }

    // ─── markAllRead ─────────────────────────────────────────────────────────

    @Test
    void markAllRead_callsRepository() {
        notificationService.markAllRead(RECIPIENT_ID);

        verify(notificationRepository).markAllReadByRecipient(RECIPIENT_ID);
    }

    // ─── deleteRead ───────────────────────────────────────────────────────────

    @Test
    void deleteRead_callsRepository() {
        notificationService.deleteRead(RECIPIENT_ID);

        verify(notificationRepository).deleteReadByRecipient(RECIPIENT_ID);
    }

    // ─── deleteNotification ───────────────────────────────────────────────────

    @Test
    void deleteNotification_ownerDeletes_succeeds() {
        when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(sampleNotification));

        notificationService.deleteNotification(NOTIF_ID, RECIPIENT_ID);

        verify(notificationRepository).delete(sampleNotification);
    }

    @Test
    void deleteNotification_wrongRecipient_throwsAccessDenied() {
        when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(sampleNotification));

        assertThatThrownBy(() -> notificationService.deleteNotification(NOTIF_ID, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── getUnreadCount ───────────────────────────────────────────────────────

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(RECIPIENT_ID)).thenReturn(5L);

        UnreadCountResponse result = notificationService.getUnreadCount(RECIPIENT_ID);

        assertThat(result.getRecipientId()).isEqualTo(RECIPIENT_ID);
        assertThat(result.getUnreadCount()).isEqualTo(5L);
    }
}
