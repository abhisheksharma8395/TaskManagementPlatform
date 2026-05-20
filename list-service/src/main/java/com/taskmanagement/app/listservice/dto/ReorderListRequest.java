package com.taskmanagement.app.listservice.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ReorderListRequest {
    @NotNull
    private List<Long> orderedListIds;   // IDs in the desired left-to-right order
}
