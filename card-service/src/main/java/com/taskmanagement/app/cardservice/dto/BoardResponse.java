package com.taskmanagement.app.cardservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardResponse {
    private Long boardId;
    private Long workspaceId;
    private String name;
    private String description;
    private String background;
    private String visibility;
    private Long createdById;
    private boolean isClosed;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}