package com.taskmanagement.app.notificationservice.controller;

import com.taskmanagement.app.notificationservice.dto.*;
import com.taskmanagement.app.notificationservice.service.NotificationService;
import com.taskmanagement.app.notificationservice.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "In-app notification management APIs")
public class NotificationController {

    @Autowired private NotificationService notificationService;
    @Autowired private JWTUtil jwtUtil;

    // ── Send (called internally by other services or admin) ───────────────────

    @PostMapping
    @Operation(summary = "Send a single notification to a recipient")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest req,
                                                     @RequestHeader(value = "Authorization", required = false) String token){
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(req));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Send a notification to multiple recipients (Platform Admin broadcast)")
    public ResponseEntity<List<NotificationResponse>> sendBulk(
            @Valid @RequestBody SendBulkNotificationRequest req , HttpServletRequest http) {
        assertPlatformAdmin(http);
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.sendBulk(req));
    }
    @GetMapping("/my")
    @Operation(summary = "Get all notifications for the authenticated user (newest first)")
    public ResponseEntity<List<NotificationResponse>> getMine(HttpServletRequest http) {
        return ResponseEntity.ok(notificationService.getByRecipient(userId(http)));
    }

    @GetMapping("/my/unread")
    @Operation(summary = "Get only unread notifications for the authenticated user")
    public ResponseEntity<List<NotificationResponse>> getUnread(HttpServletRequest http) {
        return ResponseEntity.ok(notificationService.getUnreadByRecipient(userId(http)));
    }

    @GetMapping("/my/unread-count")
    @Operation(summary = "Get unread notification badge count for top-nav display")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(HttpServletRequest http) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId(http)));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId,
                                                           HttpServletRequest http) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, userId(http)));
    }

    @PutMapping("/my/read-all")
    @Operation(summary = "Mark all notifications as read for the authenticated user")
    public ResponseEntity<String> markAllRead(HttpServletRequest http) {
        notificationService.markAllRead(userId(http));
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/my/read")
    @Operation(summary = "Delete all read notifications for the authenticated user")
    public ResponseEntity<String> deleteRead(HttpServletRequest http) {
        notificationService.deleteRead(userId(http));
        return ResponseEntity.ok("Read notifications deleted");
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a specific notification")
    public ResponseEntity<String> delete(@PathVariable Long notificationId, HttpServletRequest http) {
        notificationService.deleteNotification(notificationId, userId(http));
        return ResponseEntity.ok("Notification deleted");
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Get all notifications across the platform (Platform Admin only)")
    public ResponseEntity<List<NotificationResponse>> getAll(HttpServletRequest http) {
        assertPlatformAdmin(http);
        return ResponseEntity.ok(notificationService.getAll());
    }


    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get all notifications for a specific user (Platform Admin only)")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(@PathVariable Long recipientId,
                                                                     HttpServletRequest http) {
        assertPlatformAdmin(http);
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }

    private Long userId(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer "))
            throw new RuntimeException("Authorization header missing");
        return jwtUtil.extractUserId(h.substring(7));
    }

    private void assertPlatformAdmin(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer "))
            throw new RuntimeException("Authorization header missing");
        if (!"ADMIN".equalsIgnoreCase(jwtUtil.extractRole(h.substring(7)))) {
            throw new RuntimeException("Platform admin access required");
        }
    }
}
