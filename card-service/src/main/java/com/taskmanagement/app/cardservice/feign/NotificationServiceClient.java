package com.taskmanagement.app.cardservice.feign;

import com.taskmanagement.app.cardservice.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service", path = "/notifications", fallback = NotificationServiceClient.Fallback.class)
public interface NotificationServiceClient {

    @PostMapping
    ResponseEntity<Void> send(@RequestBody SendNotificationRequest request,
                              @RequestHeader("Authorization") String token);

    @Component
    class Fallback implements NotificationServiceClient {
        @Override
        public ResponseEntity<Void> send(SendNotificationRequest request, String token) {
            System.err.println("[CardService] Notification service unavailable. Notification dropped for recipientId: "
                    + request.getRecipientId());
            return ResponseEntity.ok().build();
        }
    }
}
