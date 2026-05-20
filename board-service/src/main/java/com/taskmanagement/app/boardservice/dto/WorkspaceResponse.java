package com.taskmanagement.app.boardservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceResponse {
    private Long workspaceId;
    private String name;
    private String description;
    private Long ownerId;
    private String visibility;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int memberCount;
}