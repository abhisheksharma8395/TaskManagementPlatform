package com.taskmanagement.app.workspaceservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWorkspaceRequest {

    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    private String name;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    @Pattern(regexp = "^(PUBLIC|PRIVATE)$", message = "Visibility must be PUBLIC or PRIVATE")
    private String visibility;

    private String logoUrl;
}
