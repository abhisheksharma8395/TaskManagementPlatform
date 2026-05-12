package com.taskmanagement.app.boardservice.feign;

import com.taskmanagement.app.boardservice.dto.WorkspaceResponse;
import com.taskmanagement.app.boardservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "workspace-service", path = "/workspaces", fallback = WorkspaceServiceClient.Fallback.class)
public interface WorkspaceServiceClient {

    @GetMapping("/{workspaceId}")
    ResponseEntity<WorkspaceResponse> getWorkspaceById(@PathVariable Long workspaceId, @RequestHeader("Authorization") String token);

    @Component
    class Fallback implements WorkspaceServiceClient {
        @Override
        public ResponseEntity<WorkspaceResponse> getWorkspaceById(Long workspaceId, String token) {
            throw new BadRequestException("Workspace service is currently unavailable. Please try again later.");
        }
    }
}
