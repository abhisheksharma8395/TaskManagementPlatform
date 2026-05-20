package com.taskmanagement.app.boardservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String avatarUrl;
    @JsonProperty("active")
    private boolean active;
}
