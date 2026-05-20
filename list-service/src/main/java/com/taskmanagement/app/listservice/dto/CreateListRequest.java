package com.taskmanagement.app.listservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateListRequest {
    @NotNull(message = "boardId is required")
    private Long boardId;
    @NotBlank(message = "List name is required")
    @Size(min = 1, max = 80)
    private String name;
    private String color;
}
