package com.taskmanagement.app.listservice.feign;

import com.taskmanagement.app.listservice.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {
    @GetMapping("/user/id/{userId}")
    UserProfileResponse getUserById(@PathVariable Long userId);
}