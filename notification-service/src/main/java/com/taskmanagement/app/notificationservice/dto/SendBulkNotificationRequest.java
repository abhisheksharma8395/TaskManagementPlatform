package com.taskmanagement.app.notificationservice.dto;
import jakarta.validation.constraints.*; import lombok.Data;
import java.util.List;
@Data
public class SendBulkNotificationRequest {
    /** If empty/null, broadcasts to ALL users (Platform Admin feature) */
    private List<Long> recipientIds;
    // ADD THIS — parallel list of emails matching recipientIds (optional)
    private List<String> recipientEmails;
    @NotBlank @Size(max = 150) private String title;
    @NotBlank @Size(max = 500) private String message;
    private String deepLinkUrl;
}
