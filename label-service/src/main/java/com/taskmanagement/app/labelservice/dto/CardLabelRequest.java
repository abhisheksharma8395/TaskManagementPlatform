package com.taskmanagement.app.labelservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class CardLabelRequest {
    @NotNull private Long cardId;
    @NotNull private Long labelId;
}
