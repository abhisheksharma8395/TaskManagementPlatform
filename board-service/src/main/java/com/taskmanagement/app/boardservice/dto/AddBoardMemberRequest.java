package com.taskmanagement.app.boardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddBoardMemberRequest {
    @NotNull(message = "userId is required")
    private Long userId;
    @Pattern(regexp = "^(OBSERVER|MEMBER|ADMIN)$", message = "Role must be OBSERVER, MEMBER or ADMIN")
    private String role = "MEMBER";
}
