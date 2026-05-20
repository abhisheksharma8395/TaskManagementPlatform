package com.taskmanagement.app.notificationservice.repository;

import com.taskmanagement.app.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, boolean isRead);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    List<Notification> findByType(String type);

    List<Notification> findByRelatedId(Long relatedId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId")
    void markAllReadByRecipient(@Param("recipientId") Long recipientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = true")
    void deleteReadByRecipient(@Param("recipientId") Long recipientId);
}
