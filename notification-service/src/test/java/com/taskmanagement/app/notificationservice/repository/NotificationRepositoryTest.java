package com.taskmanagement.app.notificationservice.repository;

import com.taskmanagement.app.notificationservice.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private final Long RECIPIENT_ID = 1L;

    @BeforeEach
    void setUp() {
        Notification read = new Notification();
        read.setRecipientId(RECIPIENT_ID);
        read.setType("ASSIGNMENT");
        read.setTitle("Read notification");
        read.setMessage("Some message");
        read.setRead(true);
        notificationRepository.save(read);

        Notification unread = new Notification();
        unread.setRecipientId(RECIPIENT_ID);
        unread.setType("COMMENT");
        unread.setTitle("Unread notification");
        unread.setMessage("Another message");
        unread.setRead(false);
        notificationRepository.save(unread);
    }

    @Test
    void findByRecipientIdOrderByCreatedAtDesc_returnsBoth() {
        List<Notification> results = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(RECIPIENT_ID);

        assertThat(results).hasSize(2);
    }

    @Test
    void findByRecipientIdAndIsReadOrderByCreatedAtDesc_unreadOnly_returnsOne() {
        List<Notification> results = notificationRepository
                .findByRecipientIdAndIsReadOrderByCreatedAtDesc(RECIPIENT_ID, false);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isRead()).isFalse();
    }

    @Test
    void countByRecipientIdAndIsReadFalse_returnsOne() {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(RECIPIENT_ID);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void markAllReadByRecipient_marksAllAsRead() {
        notificationRepository.markAllReadByRecipient(RECIPIENT_ID);

        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(RECIPIENT_ID);
        assertThat(unreadCount).isZero();
    }

    @Test
    void deleteReadByRecipient_deletesOnlyReadNotifications() {
        notificationRepository.deleteReadByRecipient(RECIPIENT_ID);

        List<Notification> remaining = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(RECIPIENT_ID);
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).isRead()).isFalse();
    }
}
