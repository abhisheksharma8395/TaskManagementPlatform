package com.taskmanagement.app.authservice.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}