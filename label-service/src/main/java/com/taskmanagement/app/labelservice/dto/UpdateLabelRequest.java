package com.taskmanagement.app.labelservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class UpdateLabelRequest {
    @Size(max=50)
    private String name;
    @Pattern(regexp="^#[0-9A-Fa-f]{6}$")
    private String color;
}
