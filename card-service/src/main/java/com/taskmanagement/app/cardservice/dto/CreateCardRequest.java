package com.taskmanagement.app.cardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateCardRequest {
    @NotNull(message = "listId is required")
    private Long listId;
    @NotNull(message = "boardId is required")
    private Long boardId;
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200)
    private String title;
    private String description;
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "Priority must be LOW, MEDIUM, HIGH or CRITICAL")
    private String priority = "MEDIUM";
    private LocalDate dueDate;
    private LocalDate startDate;
    private Long assigneeId;
    private String coverColor;
}
