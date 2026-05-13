package com.taskmanagement.app.workspaceservice.service;

import com.taskmanagement.app.workspaceservice.dto.*;
import com.taskmanagement.app.workspaceservice.entity.Workspace;
import com.taskmanagement.app.workspaceservice.entity.WorkspaceMember;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceAccessDeniedException;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceNotFoundException;
import com.taskmanagement.app.workspaceservice.exception.WorkspaceOperationException;
import com.taskmanagement.app.workspaceservice.feign.AuthServiceClient;
import com.taskmanagement.app.workspaceservice.repository.WorkspaceMemberRepository;
import com.taskmanagement.app.workspaceservice.repository.WorkspaceRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private WorkspaceMemberRepository memberRepository;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private Workspace publicWorkspace;
    private Workspace privateWorkspace;
    private WorkspaceMember adminMember;
    private final Long OWNER_ID = 1L;
    private final Long OTHER_ID = 2L;

    @BeforeEach
    void setUp() {
        publicWorkspace = new Workspace();
        publicWorkspace.setWorkspaceId(10L);
        publicWorkspace.setName("My Workspace");
        publicWorkspace.setOwnerId(OWNER_ID);
        publicWorkspace.setVisibility("PUBLIC");

        privateWorkspace = new Workspace();
        privateWorkspace.setWorkspaceId(11L);
        privateWorkspace.setName("Private WS");
        privateWorkspace.setOwnerId(OWNER_ID);
        privateWorkspace.setVisibility("PRIVATE");

        adminMember = new WorkspaceMember();
        adminMember.setMemberId(100L);
        adminMember.setWorkspace(publicWorkspace);
        adminMember.setUserId(OTHER_ID);
        adminMember.setRole("ADMIN");
    }

    // ─── createWorkspace ──────────────────────────────────────────────────────

    @Test
    void createWorkspace_happyPath_returnsResponse() {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest();
        req.setName("My Workspace");
        req.setVisibility("PUBLIC");

        when(workspaceRepository.existsByNameAndOwnerId("My Workspace", OWNER_ID)).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(publicWorkspace);
        when(memberRepository.save(any(WorkspaceMember.class))).thenReturn(adminMember);

        WorkspaceResponse result = workspaceService.createWorkspace(req, OWNER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getWorkspaceId()).isEqualTo(10L);
        verify(workspaceRepository).save(any(Workspace.class));
        verify(memberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void createWorkspace_duplicateName_throwsOperationException() {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest();
        req.setName("My Workspace");

        when(workspaceRepository.existsByNameAndOwnerId("My Workspace", OWNER_ID)).thenReturn(true);

        assertThatThrownBy(() -> workspaceService.createWorkspace(req, OWNER_ID))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("already have a workspace named");
    }

    // ─── getWorkspaceById ─────────────────────────────────────────────────────

    @Test
    void getWorkspaceById_publicWorkspace_returnsResponse() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));

        WorkspaceResponse result = workspaceService.getWorkspaceById(10L, OTHER_ID);

        assertThat(result.getWorkspaceId()).isEqualTo(10L);
    }

    @Test
    void getWorkspaceById_privateWorkspace_requesterNotMember_throwsAccessDenied() {
        when(workspaceRepository.findById(11L)).thenReturn(Optional.of(privateWorkspace));
        when(memberRepository.existsByWorkspace_WorkspaceIdAndUserId(11L, OTHER_ID)).thenReturn(false);

        assertThatThrownBy(() -> workspaceService.getWorkspaceById(11L, OTHER_ID))
                .isInstanceOf(WorkspaceAccessDeniedException.class);
    }

    @Test
    void getWorkspaceById_notFound_throwsWorkspaceNotFound() {
        when(workspaceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.getWorkspaceById(999L, OWNER_ID))
                .isInstanceOf(WorkspaceNotFoundException.class);
    }

    // ─── getWorkspacesByOwner ─────────────────────────────────────────────────

    @Test
    void getWorkspacesByOwner_returnsList() {
        when(workspaceRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(publicWorkspace));

        List<WorkspaceResponse> result = workspaceService.getWorkspacesByOwner(OWNER_ID);

        assertThat(result).hasSize(1);
    }

    // ─── getWorkspacesByMember ────────────────────────────────────────────────

    @Test
    void getWorkspacesByMember_returnsList() {
        when(workspaceRepository.findByMemberUserId(OTHER_ID)).thenReturn(List.of(publicWorkspace));

        List<WorkspaceResponse> result = workspaceService.getWorkspacesByMember(OTHER_ID);

        assertThat(result).hasSize(1);
    }

    // ─── getPublicWorkspaces ──────────────────────────────────────────────────

    @Test
    void getPublicWorkspaces_returnsList() {
        when(workspaceRepository.findByVisibility("PUBLIC")).thenReturn(List.of(publicWorkspace));

        List<WorkspaceResponse> result = workspaceService.getPublicWorkspaces();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVisibility()).isEqualTo("PUBLIC");
    }

    // ─── getAllWorkspaces ─────────────────────────────────────────────────────

    @Test
    void getAllWorkspaces_returnsList() {
        when(workspaceRepository.findAll()).thenReturn(List.of(publicWorkspace, privateWorkspace));

        List<WorkspaceResponse> result = workspaceService.getAllWorkspaces();

        assertThat(result).hasSize(2);
    }

    // ─── updateWorkspace ──────────────────────────────────────────────────────

    @Test
    void updateWorkspace_ownerUpdates_succeeds() {
        UpdateWorkspaceRequest req = new UpdateWorkspaceRequest();
        req.setName("New Name");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(publicWorkspace);

        WorkspaceResponse result = workspaceService.updateWorkspace(10L, req, OWNER_ID);

        assertThat(result).isNotNull();
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void updateWorkspace_nonAdminNonOwner_throwsAccessDenied() {
        UpdateWorkspaceRequest req = new UpdateWorkspaceRequest();

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.findByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.updateWorkspace(10L, req, OTHER_ID))
                .isInstanceOf(WorkspaceAccessDeniedException.class);
    }

    // ─── deleteWorkspace ──────────────────────────────────────────────────────

    @Test
    void deleteWorkspace_owner_succeeds() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));

        workspaceService.deleteWorkspace(10L, OWNER_ID);

        verify(workspaceRepository).delete(publicWorkspace);
    }

    @Test
    void deleteWorkspace_nonOwner_throwsAccessDenied() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));

        assertThatThrownBy(() -> workspaceService.deleteWorkspace(10L, OTHER_ID))
                .isInstanceOf(WorkspaceAccessDeniedException.class)
                .hasMessageContaining("owner");
    }

    // ─── addMember ────────────────────────────────────────────────────────────

    @Test
    void addMember_happyPath_succeeds() {
        AddMemberRequest req = new AddMemberRequest();
        req.setUserId(OTHER_ID);
        req.setRole("MEMBER");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.existsByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(false);
        UserProfileResponse profile = new UserProfileResponse();
        when(authServiceClient.getUserById(OTHER_ID)).thenReturn(profile);
        when(memberRepository.save(any(WorkspaceMember.class))).thenReturn(adminMember);

        WorkspaceMemberResponse result = workspaceService.addMember(10L, req, OWNER_ID);

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void addMember_alreadyMember_throwsOperationException() {
        AddMemberRequest req = new AddMemberRequest();
        req.setUserId(OTHER_ID);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.existsByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(true);

        assertThatThrownBy(() -> workspaceService.addMember(10L, req, OWNER_ID))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void addMember_userNotFound_throwsOperationException() {
        AddMemberRequest req = new AddMemberRequest();
        req.setUserId(OTHER_ID);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.existsByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(false);
        when(authServiceClient.getUserById(OTHER_ID)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> workspaceService.addMember(10L, req, OWNER_ID))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("does not exist");
    }

    // ─── removeMember ─────────────────────────────────────────────────────────

    @Test
    void removeMember_happyPath_succeeds() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.existsByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(true);

        workspaceService.removeMember(10L, OTHER_ID, OWNER_ID);

        verify(memberRepository).deleteByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID);
    }

    @Test
    void removeMember_cannotRemoveOwner_throwsOperationException() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));

        assertThatThrownBy(() -> workspaceService.removeMember(10L, OWNER_ID, OWNER_ID))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void removeMember_notMember_throwsOperationException() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.existsByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(false);

        assertThatThrownBy(() -> workspaceService.removeMember(10L, OTHER_ID, OWNER_ID))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("not a member");
    }

    // ─── updateMemberRole ─────────────────────────────────────────────────────

    @Test
    void updateMemberRole_happyPath_succeeds() {
        UpdateMemberRoleRequest req = new UpdateMemberRoleRequest();
        req.setRole("ADMIN");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.findByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(Optional.of(adminMember));
        when(memberRepository.save(any(WorkspaceMember.class))).thenReturn(adminMember);

        WorkspaceMemberResponse result = workspaceService.updateMemberRole(10L, OTHER_ID, req, OWNER_ID);

        assertThat(result).isNotNull();
        verify(memberRepository).save(adminMember);
    }

    @Test
    void updateMemberRole_memberNotFound_throwsOperationException() {
        UpdateMemberRoleRequest req = new UpdateMemberRoleRequest();
        req.setRole("MEMBER");

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.findByWorkspace_WorkspaceIdAndUserId(10L, OTHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.updateMemberRole(10L, OTHER_ID, req, OWNER_ID))
                .isInstanceOf(WorkspaceOperationException.class);
    }

    // ─── getMembers ───────────────────────────────────────────────────────────

    @Test
    void getMembers_publicWorkspace_returnsList() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(publicWorkspace));
        when(memberRepository.findByWorkspace_WorkspaceId(10L)).thenReturn(List.of(adminMember));

        List<WorkspaceMemberResponse> result = workspaceService.getMembers(10L, OTHER_ID);

        assertThat(result).hasSize(1);
    }
}
