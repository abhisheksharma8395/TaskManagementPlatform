package com.taskmanagement.app.cardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MoveCardRequest {
    @NotNull(message = "targetListId is required")
    private Long targetListId;
    private Integer position;    // null = append at end
}
