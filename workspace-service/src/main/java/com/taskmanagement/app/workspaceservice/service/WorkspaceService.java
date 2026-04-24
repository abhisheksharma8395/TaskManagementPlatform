package com.taskmanagement.app.workspaceservice.service;

import com.taskmanagement.app.workspaceservice.dto.*;

import java.util.List;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, Long ownerId);

    WorkspaceResponse getWorkspaceById(Long workspaceId, Long requesterId);

    List<WorkspaceResponse> getWorkspacesByOwner(Long ownerId);

    List<WorkspaceResponse> getWorkspacesByMember(Long userId);

    List<WorkspaceResponse> getPublicWorkspaces();

    WorkspaceResponse updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request, Long requesterId);

    void deleteWorkspace(Long workspaceId, Long requesterId);

    WorkspaceMemberResponse addMember(Long workspaceId, AddMemberRequest request, Long requesterId);

    void removeMember(Long workspaceId, Long userId, Long requesterId);

    WorkspaceMemberResponse updateMemberRole(Long workspaceId, Long userId, UpdateMemberRoleRequest request, Long requesterId);

    List<WorkspaceMemberResponse> getMembers(Long workspaceId, Long requesterId);
}
