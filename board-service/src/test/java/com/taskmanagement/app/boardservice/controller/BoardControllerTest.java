package com.taskmanagement.app.boardservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.app.boardservice.dto.*;
import com.taskmanagement.app.boardservice.feign.AuthServiceClient;
import com.taskmanagement.app.boardservice.config.JWTFilter;
import com.taskmanagement.app.boardservice.service.BoardService;
import com.taskmanagement.app.boardservice.util.JWTUtil;
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

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private AuthServiceClient authServiceClient;

    private final String AUTH_HEADER = "Bearer test.jwt.token";

    @BeforeEach
    void setupMocks() {
        when(jwtUtil.extractUsername(anyString())).thenReturn("john_doe");
        UserProfileResponse profile = new UserProfileResponse(1L, "john_doe", "John Doe",
                "john@example.com", "USER", null, true);
        when(authServiceClient.getUserByUsername("john_doe")).thenReturn(ResponseEntity.ok(profile));
    }

    private BoardResponse buildBoardResponse() {
        BoardResponse r = new BoardResponse();
        r.setBoardId(10L);
        r.setWorkspaceId(5L);
        r.setName("Test Board");
        r.setVisibility("PUBLIC");
        r.setCreatedById(1L);
        return r;
    }

    private BoardMemberResponse buildMemberResponse() {
        BoardMemberResponse r = new BoardMemberResponse();
        r.setBoardMemberId(100L);
        r.setBoardId(10L);
        r.setUserId(2L);
        r.setRole("MEMBER");
        return r;
    }

    @Test
    @WithMockUser
    void createBoard_validRequest_returns201() throws Exception {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setWorkspaceId(5L);
        req.setName("Test Board");
        req.setVisibility("PUBLIC");

        when(boardService.createBoard(any(CreateBoardRequest.class), anyLong())).thenReturn(buildBoardResponse());

        mockMvc.perform(post("/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").value(10));
    }

    @Test
    @WithMockUser
    void getBoardById_found_returns200() throws Exception {
        when(boardService.getBoardById(anyLong(), anyLong())).thenReturn(buildBoardResponse());

        mockMvc.perform(get("/boards/10")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Board"));
    }

    @Test
    @WithMockUser
    void getBoardsByWorkspace_returns200() throws Exception {
        when(boardService.getBoardsByWorkspace(anyLong(), anyLong())).thenReturn(List.of(buildBoardResponse()));

        mockMvc.perform(get("/boards/workspace/5")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workspaceId").value(5));
    }

    @Test
    @WithMockUser
    void getBoardsByMember_sameUser_returns200() throws Exception {
        when(boardService.getBoardsByMember(1L)).thenReturn(List.of(buildBoardResponse()));

        mockMvc.perform(get("/boards/member/1")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateBoard_validRequest_returns200() throws Exception {
        UpdateBoardRequest req = new UpdateBoardRequest();
        req.setName("Updated Board");

        when(boardService.updateBoard(anyLong(), any(UpdateBoardRequest.class), anyLong()))
                .thenReturn(buildBoardResponse());

        mockMvc.perform(put("/boards/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void closeBoard_returns200() throws Exception {
        when(boardService.closeBoard(anyLong(), anyLong())).thenReturn(buildBoardResponse());

        mockMvc.perform(put("/boards/10/close")
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteBoard_returns200() throws Exception {
        mockMvc.perform(delete("/boards/10")
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Board deleted successfully"));
    }

    @Test
    @WithMockUser
    void addMember_validRequest_returns201() throws Exception {
        AddBoardMemberRequest req = new AddBoardMemberRequest();
        req.setUserId(2L);
        req.setRole("MEMBER");

        when(boardService.addMember(anyLong(), any(AddBoardMemberRequest.class), anyLong()))
                .thenReturn(buildMemberResponse());

        mockMvc.perform(post("/boards/10/members")
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
        mockMvc.perform(delete("/boards/10/members/2")
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Member removed"));
    }

    @Test
    @WithMockUser
    void updateMemberRole_returns200() throws Exception {
        UpdateBoardMemberRoleRequest req = new UpdateBoardMemberRoleRequest();
        req.setRole("ADMIN");

        when(boardService.updateMemberRole(anyLong(), anyLong(), any(UpdateBoardMemberRoleRequest.class), anyLong()))
                .thenReturn(buildMemberResponse());

        mockMvc.perform(put("/boards/10/members/2/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("Authorization", AUTH_HEADER)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMembers_returns200() throws Exception {
        when(boardService.getMembers(anyLong(), anyLong())).thenReturn(List.of(buildMemberResponse()));

        mockMvc.perform(get("/boards/10/members")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardId").value(10));
    }
}
