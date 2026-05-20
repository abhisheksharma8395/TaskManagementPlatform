package com.taskmanagement.app.boardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateBoardMemberRoleRequest {
    @NotBlank
    @Pattern(regexp = "^(OBSERVER|MEMBER|ADMIN)$")
    private String role;
}
