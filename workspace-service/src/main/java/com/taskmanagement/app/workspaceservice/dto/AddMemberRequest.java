package com.taskmanagement.app.workspaceservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @Pattern(regexp = "^(ADMIN|MEMBER)$", message = "Role must be ADMIN or MEMBER")
    private String role = "MEMBER";
}
