package com.taskmanagement.app.cardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SetStatusRequest {
    @NotBlank
    @Pattern(regexp = "^(TO_DO|IN_PROGRESS|IN_REVIEW|DONE)$", message = "Invalid status value")
    private String status;
}
