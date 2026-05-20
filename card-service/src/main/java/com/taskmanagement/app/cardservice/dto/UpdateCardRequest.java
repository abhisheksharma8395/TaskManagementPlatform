package com.taskmanagement.app.cardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateCardRequest {
    @Size(min = 1, max = 200)
    private String title;
    private String description;
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$")
    private String priority;
    @Pattern(regexp = "^(TO_DO|IN_PROGRESS|IN_REVIEW|DONE)$", message = "Invalid status value")
    private String status;
    private LocalDate dueDate;
    private LocalDate startDate;
    private Long assigneeId;
    private String coverColor;
}
