package com.taskmanagement.app.workspaceservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String avatarUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
}
