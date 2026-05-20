package com.taskmanagement.app.boardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBoardRequest {
    @NotNull(message = "workspaceId is required")
    private Long workspaceId;
    @NotBlank(message = "Board name is required")
    @Size(min = 2, max = 80)
    private String name;
    @Size(max = 300)
    private String description;
    private String background;
    @Pattern(regexp = "^(PUBLIC|PRIVATE)$", message = "Visibility must be PUBLIC or PRIVATE")
    private String visibility = "PUBLIC";
}
