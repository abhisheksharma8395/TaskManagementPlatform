package com.taskmanagement.app.workspaceservice.controller;

import com.taskmanagement.app.workspaceservice.dto.*;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceOperationException;
import com.taskmanagement.app.workspaceservice.feign.AuthServiceClient;
import com.taskmanagement.app.workspaceservice.service.WorkspaceService;
import com.taskmanagement.app.workspaceservice.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthServiceClient authServiceClient;


    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request, HttpServletRequest httpRequest) {
        Long ownerId = extractUserId(httpRequest);
        WorkspaceResponse response = workspaceService.createWorkspace(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(@PathVariable Long workspaceId, HttpServletRequest httpRequest) {
        Long requesterId = extractUserId(httpRequest);
        return ResponseEntity.ok(workspaceService.getWorkspaceById(workspaceId, requesterId));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get all workspaces owned by a user")
    public ResponseEntity<List<WorkspaceResponse>> getByOwner(@PathVariable Long ownerId, HttpServletRequest httpRequest) {
        assertSameUserOrPlatformAdmin(ownerId, httpRequest);
        return ResponseEntity.ok(workspaceService.getWorkspacesByOwner(ownerId));
    }

    @GetMapping("/member/{userId}")
    @Operation(summary = "Get all workspaces a user is a member of")
    public ResponseEntity<List<WorkspaceResponse>> getByMember(@PathVariable Long userId, HttpServletRequest httpRequest) {
        assertSameUserOrPlatformAdmin(userId, httpRequest);
        return ResponseEntity.ok(workspaceService.getWorkspacesByMember(userId));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "List all workspaces across the platform (Platform Admin only)")
    public ResponseEntity<List<WorkspaceResponse>> getAllWorkspaces(HttpServletRequest httpRequest) {
        assertPlatformAdmin(httpRequest);
        return ResponseEntity.ok(workspaceService.getAllWorkspaces());
    }

    @GetMapping("/public")
    @Operation(summary = "List all public workspaces")
    public ResponseEntity<List<WorkspaceResponse>> getPublicWorkspaces() {
        return ResponseEntity.ok(workspaceService.getPublicWorkspaces());
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Update workspace details")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            HttpServletRequest httpRequest) {

        Long requesterId = extractUserId(httpRequest);
        return ResponseEntity.ok(workspaceService.updateWorkspace(workspaceId, request, requesterId));
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Delete a workspace (owner only)")
    public ResponseEntity<String> deleteWorkspace(
            @PathVariable Long workspaceId,
            HttpServletRequest httpRequest) {

        Long requesterId = extractUserId(httpRequest);
        workspaceService.deleteWorkspace(workspaceId, requesterId);
        return ResponseEntity.ok("Workspace deleted successfully");
    }


    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "Add a member to a workspace")
    public ResponseEntity<WorkspaceMemberResponse> addMember(
            @PathVariable Long workspaceId,
            @Valid @RequestBody AddMemberRequest request,
            HttpServletRequest httpRequest) {

        Long requesterId = extractUserId(httpRequest);
        WorkspaceMemberResponse response = workspaceService.addMember(workspaceId, request, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Remove a member from a workspace")
    public ResponseEntity<String> removeMember(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {

        Long requesterId = extractUserId(httpRequest);
        workspaceService.removeMember(workspaceId, userId, requesterId);
        return ResponseEntity.ok("Member removed successfully");
    }

    @PutMapping("/{workspaceId}/members/{userId}/role")
    @Operation(summary = "Update a member's role in a workspace")
    public ResponseEntity<WorkspaceMemberResponse> updateMemberRole(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            HttpServletRequest httpRequest) {

        Long requesterId = extractUserId(httpRequest);
        return ResponseEntity.ok(workspaceService.updateMemberRole(workspaceId, userId, request, requesterId));
    }

    @GetMapping("/{workspaceId}/members")
    @Operation(summary = "Get all members of a workspace")
    public ResponseEntity<List<WorkspaceMemberResponse>> getMembers(
            @PathVariable Long workspaceId,
            HttpServletRequest httpRequest) {

        Long requesterId = extractUserId(httpRequest);
        return ResponseEntity.ok(workspaceService.getMembers(workspaceId, requesterId));
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing or malformed");
        }
        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);  // username IS in the token
        ResponseEntity<UserProfileResponse> response = authServiceClient.getUserByUsername(username);
        if (response == null || response.getBody() == null || response.getBody().getUserId() == null) {
            throw new WorkspaceOperationException("Auth service is unavailable. Please try again later.");
        }
        return response.getBody().getUserId();

    }

    private void assertPlatformAdmin(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing or malformed");
        }

        String role = jwtUtil.extractRole(header.substring(7));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Platform admin access required");
        }
    }

    private void assertSameUserOrPlatformAdmin(Long userId, HttpServletRequest request) {
        Long requesterId = extractUserId(request);
        if (requesterId.equals(userId)) return;

        String header = request.getHeader("Authorization");
        String role = jwtUtil.extractRole(header.substring(7));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("You can only view your own workspace membership");
        }
    }
}
