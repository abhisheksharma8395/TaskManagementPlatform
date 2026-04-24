package com.taskmanagement.app.workspaceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    private String name;

    private String description;

    @Pattern(regexp = "^(PUBLIC|PRIVATE)$", message = "Visibility must be PUBLIC or PRIVATE")
    private String visibility = "PUBLIC";

    private String logoUrl;
}
