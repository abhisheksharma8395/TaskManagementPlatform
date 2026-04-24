package com.taskmanagement.app.workspaceservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(
    name = "workspace_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
)
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    // Many user can have one workspace
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // userId from auth-service
    @Column(nullable = false)
    private Long userId;

    // Two Role -> Member and Admin
    @Column(nullable = false)
    private String role = "MEMBER";

    @Column(nullable = false)
    private LocalDate joinedAt;

    @PrePersist
    public void onCreate() {
        joinedAt = LocalDate.now();
    }
}
