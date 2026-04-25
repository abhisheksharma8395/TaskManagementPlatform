package com.taskmanagement.app.boardservice.feign;

import com.taskmanagement.app.boardservice.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {
    @GetMapping("/user/id/{userId}")
    UserProfileResponse getUserById(@PathVariable Long userId);

    @GetMapping("/user/username/{username}")
    ResponseEntity<UserProfileResponse> getUserByUsername(@PathVariable String username);
}