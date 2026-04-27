package com.taskmanagement.app.cardservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListResponse {
    private Long listId;
    private Long boardId;
    private String name;
    private Integer position;
    private String color;
    private boolean isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}