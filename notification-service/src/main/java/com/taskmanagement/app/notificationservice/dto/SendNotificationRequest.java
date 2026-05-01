package com.taskmanagement.app.notificationservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
@Data
public class SendNotificationRequest {
    @NotNull(message = "recipientId is required")
    private Long recipientId;
    // ADD THIS — the email address to send to (optional, only used for ASSIGNMENT and DUE_DATE)
    private String recipientEmail;
    private Long actorId;
    @NotBlank(message = "type is required")
    @Pattern(regexp = "^(ASSIGNMENT|MENTION|DUE_DATE|COMMENT|MOVE|BROADCAST)$",
             message = "type must be ASSIGNMENT, MENTION, DUE_DATE, COMMENT, MOVE or BROADCAST")
    private String type;
    @NotBlank(message = "title is required") @Size(max = 150)
    private String title;
    @NotBlank(message = "message is required") @Size(max = 500)
    private String message;
    private Long relatedId;
    private String relatedType;    // CARD or BOARD
    private String deepLinkUrl;
}
