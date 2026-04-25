package com.taskmanagement.app.boardservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "board_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "user_id"}))
public class BoardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private Long userId;

    // OBSERVER or MEMBER or ADMIN
    @Column(nullable = false)
    private String role = "MEMBER";

    @Column(nullable = false)
    private LocalDate addedAt;

    @PrePersist
    void onCreate() { addedAt = LocalDate.now(); }
}
