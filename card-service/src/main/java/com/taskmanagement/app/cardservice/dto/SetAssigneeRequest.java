package com.taskmanagement.app.cardservice.dto;
import lombok.Data;

@Data
public class SetAssigneeRequest {
    private Long assigneeId;   // null = unassign
}
