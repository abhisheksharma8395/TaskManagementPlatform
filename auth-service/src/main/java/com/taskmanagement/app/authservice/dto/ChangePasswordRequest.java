package com.taskmanagement.app.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ChangePasswordRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
}
