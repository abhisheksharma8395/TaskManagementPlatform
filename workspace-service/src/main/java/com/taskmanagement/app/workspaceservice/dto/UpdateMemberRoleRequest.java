package com.taskmanagement.app.workspaceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|MEMBER)$", message = "Role must be ADMIN or MEMBER")
    private String role;
}
