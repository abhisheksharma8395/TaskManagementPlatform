package com.taskmanagement.app.cardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SetPriorityRequest {
    @NotBlank
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$")
    private String priority;
}
