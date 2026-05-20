package com.taskmanagement.app.listservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MoveListRequest {
    @NotNull(message = "targetBoardId is required")
    private Long targetBoardId;
    private Integer position;            // null = append at end
}
