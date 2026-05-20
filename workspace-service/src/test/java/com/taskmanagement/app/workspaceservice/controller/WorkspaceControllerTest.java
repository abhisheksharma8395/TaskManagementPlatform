package com.taskmanagement.app.workspaceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.app.workspaceservice.config.JWTFilter;
import com.taskmanagement.app.workspaceservice.dto.*;
import com.taskmanagement.app.workspaceservice.feign.AuthServiceClient;
import com.taskmanagement.app.workspaceservice.service.WorkspaceService;
import com.taskmanagement.app.workspaceservice.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkspaceService workspaceService;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private AuthServiceClient authServiceClient;

    private final String AUTH_HEADER = "Bearer test.jwt.token";

    @BeforeEach
    void setupMocks() {
        when(jwtUtil.extractUsername(anyString())).thenReturn("john_doe");
        UserProfileResponse profile = new UserProfileResponse();
        profile.setUserId(1L);
        profile.setUsername("john_doe");
        when(authServiceClient.getUserByUsername("john_doe")).thenReturn(ResponseEntity.ok(profile));
    }

    private WorkspaceResponse buildWorkspaceResponse() {
        WorkspaceResponse r = new WorkspaceResponse();
        r.setWorkspaceId(10L);
        r.setName("Test Workspace");
        r.setVisibility("PUBLIC");
        r.setOwnerId(1L);
        return r;
    }

    private WorkspaceMemberResponse buildMemberResponse() {
        WorkspaceMemberResponse r = new WorkspaceMemberResponse();
        r.setMemberId(100L);
        r.setWorkspaceId(10L);
        r.setUserId(2L);
        r.setRole("MEMBER");
        return r;
    }

    @Test
    @WithMockUser
    void createWorkspace_validRequest_returns201() throws Exception {
        CreateWorkspaceRequest req = new CreateWorkspaceRequest();
        req.setName("Test Workspace");
        req.setVisibility("PUBLIC");

        when(workspaceService.createWorkspace(any(CreateWorkspaceRequest.class), anyLong()))
                .thenReturn(buildWorkspaceResponse());

        mockMvc.perform(post("/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workspaceId").value(10));
    }

    @Test
    @WithMockUser
    void getWorkspaceById_found_returns200() throws Exception {
        when(workspaceService.getWorkspaceById(anyLong(), anyLong())).thenReturn(buildWorkspaceResponse());

        mockMvc.perform(get("/workspaces/10")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Workspace"));
    }

    @Test
    @WithMockUser
    void getPublicWorkspaces_returns200() throws Exception {
        when(workspaceService.getPublicWorkspaces()).thenReturn(List.of(buildWorkspaceResponse()));

        mockMvc.perform(get("/workspaces/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"));
    }

    @Test
    @WithMockUser
    void updateWorkspace_validRequest_returns200() throws Exception {
        UpdateWorkspaceRequest req = new UpdateWorkspaceRequest();
        req.setName("Updated");

        when(workspaceService.updateWorkspace(anyLong(), any(UpdateWorkspaceRequest.class), anyLong()))
                .thenReturn(buildWorkspaceResponse());

        mockMvc.perform(put("/workspaces/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteWorkspace_returns200() throws Exception {
        mockMvc.perform(delete("/workspaces/10")
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Workspace deleted successfully"));
    }

    @Test
    @WithMockUser
    void addMember_validRequest_returns201() throws Exception {
        AddMemberRequest req = new AddMemberRequest();
        req.setUserId(2L);
        req.setRole("MEMBER");

        when(workspaceService.addMember(anyLong(), any(AddMemberRequest.class), anyLong()))
                .thenReturn(buildMemberResponse());

        mockMvc.perform(post("/workspaces/10/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    @WithMockUser
    void removeMember_returns200() throws Exception {
        mockMvc.perform(delete("/workspaces/10/members/2")
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Member removed successfully"));
    }

    @Test
    @WithMockUser
    void updateMemberRole_returns200() throws Exception {
        UpdateMemberRoleRequest req = new UpdateMemberRoleRequest();
        req.setRole("ADMIN");

        when(workspaceService.updateMemberRole(anyLong(), anyLong(), any(UpdateMemberRoleRequest.class), anyLong()))
                .thenReturn(buildMemberResponse());

        mockMvc.perform(put("/workspaces/10/members/2/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMembers_returns200() throws Exception {
        when(workspaceService.getMembers(anyLong(), anyLong())).thenReturn(List.of(buildMemberResponse()));

        mockMvc.perform(get("/workspaces/10/members")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workspaceId").value(10));
    }
}
