package com.taskmanagement.app.workspaceservice.service;

import com.taskmanagement.app.workspaceservice.feign.AuthServiceClient;
import com.taskmanagement.app.workspaceservice.dto.*;
import com.taskmanagement.app.workspaceservice.entity.Workspace;
import com.taskmanagement.app.workspaceservice.entity.WorkspaceMember;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceAccessDeniedException;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceNotFoundException;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceOperationException;
import com.taskmanagement.app.workspaceservice.messaging.NotificationPublisher;
import com.taskmanagement.app.workspaceservice.repository.WorkspaceMemberRepository;
import com.taskmanagement.app.workspaceservice.repository.WorkspaceRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository memberRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, Long ownerId) {
        if (workspaceRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
            throw new WorkspaceOperationException(
                    "You already have a workspace named '" + request.getName() + "'");
        }

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setOwnerId(ownerId);
        workspace.setVisibility(
                request.getVisibility() != null ? request.getVisibility().toUpperCase() : "PUBLIC");
        workspace.setLogoUrl(request.getLogoUrl());
        workspace = workspaceRepository.save(workspace);

        // Auto-add the creator as an ADMIN member
        WorkspaceMember ownerMember = new WorkspaceMember();
        ownerMember.setWorkspace(workspace);
        ownerMember.setUserId(ownerId);
        ownerMember.setRole("ADMIN");
        memberRepository.save(ownerMember);

        return mapToWorkspaceResponse(workspace);
    }

    @Override
    public WorkspaceResponse getWorkspaceById(Long workspaceId, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        assertCanView(workspace, requesterId);
        return mapToWorkspaceResponse(workspace);
    }

    @Override
    public List<WorkspaceResponse> getWorkspacesByOwner(Long ownerId) {
        return workspaceRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponse> getWorkspacesByMember(Long userId) {
        return workspaceRepository.findByMemberUserId(userId)
                .stream()
                .map(this::mapToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponse> getPublicWorkspaces() {
        return workspaceRepository.findByVisibility("PUBLIC")
                .stream()
                .map(this::mapToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkspaceResponse> getAllWorkspaces() {
        return workspaceRepository.findAll()
                .stream()
                .map(this::mapToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        assertIsAdminOrOwner(workspace, requesterId);

        if (request.getName() != null && !request.getName().isBlank()) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            workspace.setVisibility(request.getVisibility().toUpperCase());
        }
        if (request.getLogoUrl() != null) {
            workspace.setLogoUrl(request.getLogoUrl());
        }

        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional
    public void deleteWorkspace(Long workspaceId, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        if (!workspace.getOwnerId().equals(requesterId)) {
            throw new WorkspaceAccessDeniedException("Only the workspace owner can delete the workspace");
        }
        workspaceRepository.delete(workspace);
    }

    // ── Member management ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkspaceMemberResponse addMember(Long workspaceId, AddMemberRequest request, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        assertIsAdminOrOwner(workspace, requesterId);


        UserProfileResponse recipientUser;
        try {
            recipientUser = authServiceClient.getUserByEmail(request.getEmail()).getBody();
        } catch (FeignException.NotFound e) {
            throw new WorkspaceOperationException("User with email " + request.getEmail() + " does not exist");
        } catch (FeignException e) {
            throw new WorkspaceOperationException("Could not verify user existence: " + e.getMessage());
        }

        if (memberRepository.existsByWorkspace_WorkspaceIdAndUserId(workspaceId, recipientUser.getUserId())) {
            throw new WorkspaceOperationException("User " + recipientUser.getEmail() + " is already a member of this workspace");
        }


        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUserId(recipientUser.getUserId());
        member.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "MEMBER");

        WorkspaceMemberResponse saved = mapToMemberResponse(memberRepository.save(member));


        // Notify the newly assigned user — publish to RabbitMQ
        NotificationEvent event = new NotificationEvent();
        event.setRecipientId(recipientUser.getUserId());
        event.setRecipientEmail(recipientUser.getEmail());
        event.setActorId(requesterId);
        event.setType("WORKSPACE_MEMBER_ADDED");
        event.setTitle("You have been added to a workspace");
        event.setMessage("You have been added to workspace : " + workspace.getName());
        event.setRelatedId(workspaceId);
        event.setRelatedType("WORKSPACES");
        event.setDeepLinkUrl("/workspaces/" + workspace.getWorkspaceId());
        notificationPublisher.publish(event);

        return saved;

    }

    @Override
    @Transactional
    public void removeMember(Long workspaceId, Long userId, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        assertIsAdminOrOwner(workspace, requesterId);

        if (workspace.getOwnerId().equals(userId)) {
            throw new WorkspaceOperationException("Cannot remove the workspace owner from the workspace");
        }

        if (!memberRepository.existsByWorkspace_WorkspaceIdAndUserId(workspaceId, userId)) {
            throw new WorkspaceOperationException("User " + userId + " is not a member of this workspace");
        }
        memberRepository.deleteByWorkspace_WorkspaceIdAndUserId(workspaceId, userId);

        NotificationEvent event = new NotificationEvent();
        event.setRecipientId(userId);
        event.setRecipientEmail(null);
        event.setActorId(requesterId);
        event.setType("WORKSPACE_MEMBER_REMOVED");
        event.setTitle("You have been removed from a workspace");
        event.setMessage("You have been removed from workspace: " + workspace.getName());
        event.setRelatedId(workspaceId);
        event.setRelatedType("WORKSPACE");
        event.setDeepLinkUrl(null);                // no link — they no longer have access
        notificationPublisher.publish(event);
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updateMemberRole(Long workspaceId, Long userId,
                                                    UpdateMemberRoleRequest request, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        assertIsAdminOrOwner(workspace, requesterId);

        WorkspaceMember member = memberRepository
                .findByWorkspace_WorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new WorkspaceOperationException(
                        "User " + userId + " is not a member of workspace " + workspaceId));

        member.setRole(request.getRole().toUpperCase());
        return mapToMemberResponse(memberRepository.save(member));
    }

    @Override
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId, Long requesterId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        assertCanView(workspace, requesterId);

        return memberRepository.findByWorkspace_WorkspaceId(workspaceId)
                .stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Workspace findWorkspaceOrThrow(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id: " + workspaceId));
    }


    private void assertCanView(Workspace workspace, Long requesterId) {
        if ("PUBLIC".equalsIgnoreCase(workspace.getVisibility())) return;
        if (workspace.getOwnerId().equals(requesterId)) return;
        if (memberRepository.existsByWorkspace_WorkspaceIdAndUserId(workspace.getWorkspaceId(), requesterId)) return;
        throw new WorkspaceAccessDeniedException("You do not have access to this workspace");
    }

    /**
     * Only the workspace owner OR an ADMIN member may modify the workspace.
     */
    private void assertIsAdminOrOwner(Workspace workspace, Long requesterId) {
        if (workspace.getOwnerId().equals(requesterId)) return;
        memberRepository.findByWorkspace_WorkspaceIdAndUserId(workspace.getWorkspaceId(), requesterId)
                .filter(m -> "ADMIN".equalsIgnoreCase(m.getRole()))
                .orElseThrow(() -> new WorkspaceAccessDeniedException(
                        "Only workspace admins or the owner can perform this action"));
    }

    private WorkspaceResponse mapToWorkspaceResponse(Workspace w) {
        WorkspaceResponse response = new WorkspaceResponse();
        response.setWorkspaceId(w.getWorkspaceId());
        response.setName(w.getName());
        response.setDescription(w.getDescription());
        response.setOwnerId(w.getOwnerId());
        response.setVisibility(w.getVisibility());
        response.setLogoUrl(w.getLogoUrl());
        response.setCreatedAt(w.getCreatedAt());
        response.setUpdatedAt(w.getUpdatedAt());
        response.setMemberCount(w.getMembers() != null ? w.getMembers().size() : 0);
        return response;
    }

    private WorkspaceMemberResponse mapToMemberResponse(WorkspaceMember m) {
        WorkspaceMemberResponse response = new WorkspaceMemberResponse();
        response.setMemberId(m.getMemberId());
        response.setWorkspaceId(m.getWorkspace().getWorkspaceId());
        response.setUserId(m.getUserId());
        response.setRole(m.getRole());
        response.setJoinedAt(m.getJoinedAt());
        return response;
    }
}

