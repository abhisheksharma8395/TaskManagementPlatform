package com.taskmanagement.app.workspaceservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotNull(message = "email is required")
    @Email
    private String email;

    @Pattern(regexp = "^(ADMIN|MEMBER)$", message = "Role must be ADMIN or MEMBER")
    private String role = "MEMBER";
}
