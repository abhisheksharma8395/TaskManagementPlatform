package com.taskmanagement.app.labelservice.dto;
import lombok.Data; import java.time.LocalDate;
@Data public class ChecklistItemResponse {
    private Long itemId;
    private Long checklistId;
    private String text;
    private boolean isCompleted;
    private Long assigneeId;
    private LocalDate dueDate;
}
