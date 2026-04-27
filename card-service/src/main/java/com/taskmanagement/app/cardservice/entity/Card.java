package com.taskmanagement.app.cardservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column(nullable = false)
    private Long listId;

    @Column(nullable = false)
    private Long boardId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Top-to-bottom order within the list */
    @Column(nullable = false)
    private Integer position;

    /** LOW / MEDIUM / HIGH / CRITICAL */
    @Column(nullable = false)
    private String priority = "MEDIUM";

    /** TO_DO / IN_PROGRESS / IN_REVIEW / DONE */
    @Column(nullable = false)
    private String status = "TO_DO";

    private LocalDate dueDate;

    private LocalDate startDate;

    /** userId from auth-service */
    private Long assigneeId;

    @Column(nullable = false)
    private Long createdById;

    @Column(nullable = false)
    private boolean isArchived = false;

    /** Hex colour for the card cover banner */
    private String coverColor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
