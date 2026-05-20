package com.taskmanagement.app.cardservice.dto;
import lombok.Data;

@Data
public class SetAssigneeRequest {
    private String fullName;   // null = unassign
    private String email;      // unique email to resolve collisions
}
