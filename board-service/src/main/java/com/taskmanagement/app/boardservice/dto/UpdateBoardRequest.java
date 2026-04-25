package com.taskmanagement.app.boardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateBoardRequest {
    @Size(min = 2, max = 80)
    private String name;
    @Size(max = 300)
    private String description;
    private String background;
    @Pattern(regexp = "^(PUBLIC|PRIVATE)$")
    private String visibility;
}
