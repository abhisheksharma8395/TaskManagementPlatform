package com.taskmanagement.app.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "notifications", indexes = {
        @Index(name = "idx_recipient", columnList = "recipient_id"),
        @Index(name = "idx_recipient_read", columnList = "recipient_id, is_read")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long recipientId;

    /** The user who triggered the action (null for system-generated) */
    private Long actorId;


    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** ID of the related card or board */
    private Long relatedId;

    /** "CARD" or "BOARD" — used to build the deep-link URL */
    private String relatedType;

    /** Deep-link URL to navigate directly to the related entity */
    private String deepLinkUrl;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
