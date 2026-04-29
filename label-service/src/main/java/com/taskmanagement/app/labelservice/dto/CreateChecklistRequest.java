package com.taskmanagement.app.labelservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class CreateChecklistRequest {
    @NotNull private Long cardId;
    @NotBlank @Size(max=100) private String title;
    private Integer position;
}
