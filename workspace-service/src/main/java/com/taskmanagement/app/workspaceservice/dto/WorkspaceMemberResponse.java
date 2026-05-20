package com.taskmanagement.app.workspaceservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkspaceMemberResponse {
    private Long memberId;
    private Long workspaceId;
    private Long userId;
    private String role;
    private LocalDate joinedAt;
}
