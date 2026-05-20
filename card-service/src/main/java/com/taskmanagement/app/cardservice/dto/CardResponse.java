package com.taskmanagement.app.cardservice.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardResponse {
    private Long cardId;
    private Long listId;
    private Long boardId;
    private String title;
    private String description;
    private Integer position;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private LocalDate startDate;
    private Long assigneeId;
    private Long createdById;
    private boolean isArchived;
    private boolean isOverdue;
    private String coverColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
