package com.taskmanagement.app.boardservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Column(nullable = false)
    private Long workspaceId;

    @Column(nullable = false)
    private String name;

    private String description;

    // Store the hex-code of color in background
    private String background;

    // Private or Public
    @Column(nullable = false)
    private String visibility = "PUBLIC";

    @Column(nullable = false)
    private Long createdById;

    // If it is closed than no new card you can add into the board
    @Column(nullable = false)
    private boolean isClosed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BoardMember> members = new ArrayList<>();

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
