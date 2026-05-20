package com.taskmanagement.app.listservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateListRequest {
    @Size(min = 1, max = 80)
    private String name;
    private String color;
}
