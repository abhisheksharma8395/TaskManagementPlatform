package com.taskmanagement.app.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    @JsonProperty("active")
    private boolean isActive;
}