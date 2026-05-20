package com.taskmanagement.app.cardservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ReorderCardsRequest {
    @NotNull
    private List<Long> orderedCardIds;
}
