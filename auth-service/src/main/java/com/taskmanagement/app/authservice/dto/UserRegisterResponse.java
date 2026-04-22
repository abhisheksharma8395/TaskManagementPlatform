package com.taskmanagement.app.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegisterResponse {
    private String userName;
    private String fullName;
    private String email;
    private String password;
    private String role;
}
