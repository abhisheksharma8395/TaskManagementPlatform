package com.taskmanagement.app.cardservice.feign;

import com.taskmanagement.app.cardservice.dto.ListResponse;
import com.taskmanagement.app.cardservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "list-service", path = "/lists", fallback = ListServiceClient.Fallback.class)
public interface ListServiceClient {

    @GetMapping("/{listId}")
    ResponseEntity<ListResponse> getById(@PathVariable Long listId, @RequestHeader("Authorization") String token);

    @Component
    class Fallback implements ListServiceClient {
        @Override
        public ResponseEntity<ListResponse> getById(Long listId, String token) {
            throw new BadRequestException("List service is currently unavailable. Please try again later.");
        }
    }
}
