package com.taskmanagement.app.boardservice.feign;

import com.taskmanagement.app.boardservice.dto.SendNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service", path = "/notifications")
public interface NotificationServiceClient {

    @PostMapping
    ResponseEntity<Void> send(@RequestBody SendNotificationRequest request,
                              @RequestHeader("Authorization") String token);
}
