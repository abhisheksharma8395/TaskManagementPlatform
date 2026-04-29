package com.taskmanagement.app.labelservice.dto;
import lombok.Data; import java.time.LocalDate;
@Data public class LabelResponse {
    private Long labelId;
    private Long boardId;
    private String name;
    private String color;
    private LocalDate createdAt;
}
