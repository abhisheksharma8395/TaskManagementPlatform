package com.taskmanagement.app.boardservice.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BoardMemberResponse {
    private Long boardMemberId;
    private Long boardId;
    private Long userId;
    private String role;
    private LocalDate addedAt;
}
