package com.taskmanagement.app.labelservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class CreateLabelRequest {
    @NotNull private Long boardId;
    @NotBlank @Size(max=50)
    private String name;
    @NotBlank @Pattern(regexp="^#[0-9A-Fa-f]{6}$", message="Color must be a valid hex e.g. #FF5733")
    private String color;
}
