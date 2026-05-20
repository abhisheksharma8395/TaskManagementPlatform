package com.taskmanagement.app.labelservice.dto;
import jakarta.validation.constraints.*; import lombok.Data; import java.time.LocalDate;
@Data public class AddChecklistItemRequest {
    @NotBlank @Size(max=200) private String text;
    private Long assigneeId;
    private LocalDate dueDate;
}
