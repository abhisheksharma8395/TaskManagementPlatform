package com.taskmanagement.app.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
