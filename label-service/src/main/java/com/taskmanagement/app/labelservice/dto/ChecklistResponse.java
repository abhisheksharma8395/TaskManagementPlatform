package com.taskmanagement.app.labelservice.dto;
import lombok.Data; import java.time.LocalDateTime; import java.util.List;
@Data public class ChecklistResponse {
    private Long checklistId;
    private Long cardId;
    private String title;
    private Integer position;
    private int completedCount;
    private int totalCount;
    private int progressPercent;
    private LocalDateTime createdAt;
    private List<ChecklistItemResponse> items;
}
