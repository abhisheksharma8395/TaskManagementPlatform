package com.taskmanagement.app.boardservice.feign;

import com.taskmanagement.app.boardservice.dto.WorkspaceResponse;
import com.taskmanagement.app.boardservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "workspace-service", path = "/workspaces")
public interface WorkspaceServiceClient {

    @GetMapping("/{workspaceId}")
    ResponseEntity<WorkspaceResponse> getWorkspaceById(@PathVariable Long workspaceId, @RequestHeader("Authorization") String token);

}
