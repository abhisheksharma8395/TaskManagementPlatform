package com.taskmanagement.app.cardservice.feign;

import com.taskmanagement.app.cardservice.dto.UserProfileResponse;
import com.taskmanagement.app.cardservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {

    @GetMapping("/user/id/{userId}")
    UserProfileResponse getUserById(@PathVariable Long userId);

    @GetMapping("/user/username/{username}")
    ResponseEntity<UserProfileResponse> getUserByUsername(@PathVariable String username);

}
