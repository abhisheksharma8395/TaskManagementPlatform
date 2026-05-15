package com.taskmanagement.app.boardservice.service;

import com.taskmanagement.app.boardservice.dto.*;
import com.taskmanagement.app.boardservice.entity.Board;
import com.taskmanagement.app.boardservice.entity.BoardMember;
import com.taskmanagement.app.boardservice.exception.AccessDeniedException;
import com.taskmanagement.app.boardservice.exception.BadRequestException;
import com.taskmanagement.app.boardservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.boardservice.feign.AuthServiceClient;
import com.taskmanagement.app.boardservice.feign.WorkspaceServiceClient;
import com.taskmanagement.app.boardservice.repository.BoardMemberRepository;
import com.taskmanagement.app.boardservice.repository.BoardRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private BoardMemberRepository memberRepository;
    @Mock
    private WorkspaceServiceClient workspaceServiceClient;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private BoardServiceImpl boardService;

    private Board publicBoard;
    private Board privateBoard;
    private BoardMember adminMember;
    private final Long CREATOR_ID = 1L;
    private final Long OTHER_ID = 2L;

    @BeforeEach
    void setUp() {
        publicBoard = new Board();
        publicBoard.setBoardId(10L);
        publicBoard.setWorkspaceId(5L);
        publicBoard.setName("Public Board");
        publicBoard.setVisibility("PUBLIC");
        publicBoard.setCreatedById(CREATOR_ID);

        privateBoard = new Board();
        privateBoard.setBoardId(11L);
        privateBoard.setWorkspaceId(5L);
        privateBoard.setName("Private Board");
        privateBoard.setVisibility("PRIVATE");
        privateBoard.setCreatedById(CREATOR_ID);

        adminMember = new BoardMember();
        adminMember.setBoardMemberId(100L);
        adminMember.setBoard(publicBoard);
        adminMember.setUserId(OTHER_ID);
        adminMember.setRole("ADMIN");
    }

    // ─── createBoard ──────────────────────────────────────────────────────────

    @Test
    void createBoard_happyPath_returnsResponse() {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setWorkspaceId(5L);
        req.setName("New Board");
        req.setVisibility("PUBLIC");

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");
        WorkspaceResponse ws = new WorkspaceResponse();
        when(workspaceServiceClient.getWorkspaceById(5L, "Bearer token123")).thenReturn(ResponseEntity.ok(ws));
        when(boardRepository.save(any(Board.class))).thenReturn(publicBoard);
        when(memberRepository.save(any(BoardMember.class))).thenReturn(adminMember);

        BoardResponse result = boardService.createBoard(req, CREATOR_ID);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Public Board");
        verify(boardRepository).save(any(Board.class));
        verify(memberRepository).save(any(BoardMember.class));
    }

    @Test
    void createBoard_workspaceNotFound_throwsBadRequest() {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setWorkspaceId(5L);
        req.setName("New Board");

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");
        when(workspaceServiceClient.getWorkspaceById(5L, "Bearer token123"))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> boardService.createBoard(req, CREATOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Workspace not found");
    }

    @Test
    void createBoard_workspaceUnavailable_throwsBadRequest() {
        CreateBoardRequest req = new CreateBoardRequest();
        req.setWorkspaceId(5L);
        req.setName("New Board");

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");
        when(workspaceServiceClient.getWorkspaceById(5L, "Bearer token123"))
                .thenThrow(new RuntimeException("service down"));

        assertThatThrownBy(() -> boardService.createBoard(req, CREATOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unavailable");
    }

    // ─── getBoardById ─────────────────────────────────────────────────────────

    @Test
    void getBoardById_publicBoard_returnsResponse() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));

        BoardResponse result = boardService.getBoardById(10L, OTHER_ID);

        assertThat(result.getBoardId()).isEqualTo(10L);
    }

    @Test
    void getBoardById_privateBoard_requesterNotMember_throwsAccessDenied() {
        when(boardRepository.findById(11L)).thenReturn(Optional.of(privateBoard));
        when(memberRepository.existsByBoard_BoardIdAndUserId(11L, OTHER_ID)).thenReturn(false);

        assertThatThrownBy(() -> boardService.getBoardById(11L, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getBoardById_notFound_throwsResourceNotFound() {
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardById(999L, CREATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getBoardsByWorkspace ─────────────────────────────────────────────────

    @Test
    void getBoardsByWorkspace_returnsFilteredList() {
        when(boardRepository.findByWorkspaceId(5L)).thenReturn(List.of(publicBoard));

        List<BoardResponse> result = boardService.getBoardsByWorkspace(5L, OTHER_ID);

        assertThat(result).hasSize(1);
    }

    // ─── getBoardsByMember ────────────────────────────────────────────────────

    @Test
    void getBoardsByMember_returnsList() {
        when(boardRepository.findByMemberUserId(CREATOR_ID)).thenReturn(List.of(publicBoard));

        List<BoardResponse> result = boardService.getBoardsByMember(CREATOR_ID);

        assertThat(result).hasSize(1);
    }

    // ─── updateBoard ──────────────────────────────────────────────────────────

    @Test
    void updateBoard_creatorUpdates_succeeds() {
        UpdateBoardRequest req = new UpdateBoardRequest();
        req.setName("Updated Name");

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(publicBoard);

        BoardResponse result = boardService.updateBoard(10L, req, CREATOR_ID);

        assertThat(result).isNotNull();
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void updateBoard_closedBoard_throwsBadRequest() {
        publicBoard.setClosed(true);
        UpdateBoardRequest req = new UpdateBoardRequest();

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));

        assertThatThrownBy(() -> boardService.updateBoard(10L, req, CREATOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void updateBoard_nonAdminNonCreator_throwsAccessDenied() {
        UpdateBoardRequest req = new UpdateBoardRequest();
        req.setName("Changed");

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.findByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.updateBoard(10L, req, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── closeBoard ───────────────────────────────────────────────────────────

    @Test
    void closeBoard_creator_succeeds() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(publicBoard);

        BoardResponse result = boardService.closeBoard(10L, CREATOR_ID);

        assertThat(result).isNotNull();
        verify(boardRepository).save(any(Board.class));
    }

    // ─── deleteBoard ──────────────────────────────────────────────────────────

    @Test
    void deleteBoard_creator_succeeds() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));

        boardService.deleteBoard(10L, CREATOR_ID);

        verify(boardRepository).delete(publicBoard);
    }

    @Test
    void deleteBoard_nonCreator_throwsAccessDenied() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));

        assertThatThrownBy(() -> boardService.deleteBoard(10L, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("creator");
    }

    // ─── addMember ────────────────────────────────────────────────────────────

    @Test
    void addMember_happyPath_returnsResponse() {
        AddBoardMemberRequest req = new AddBoardMemberRequest();
        req.setUserId(OTHER_ID);
        req.setRole("MEMBER");

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.existsByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(false);
        when(memberRepository.save(any(BoardMember.class))).thenReturn(adminMember);

        BoardMemberResponse result = boardService.addMember(10L, req, CREATOR_ID);

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(BoardMember.class));
    }

    @Test
    void addMember_alreadyMember_throwsBadRequest() {
        AddBoardMemberRequest req = new AddBoardMemberRequest();
        req.setUserId(OTHER_ID);

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.existsByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(true);

        assertThatThrownBy(() -> boardService.addMember(10L, req, CREATOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already a member");
    }

    // ─── removeMember ─────────────────────────────────────────────────────────

    @Test
    void removeMember_happyPath_succeeds() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.existsByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(true);

        boardService.removeMember(10L, OTHER_ID, CREATOR_ID);

        verify(memberRepository).deleteByBoard_BoardIdAndUserId(10L, OTHER_ID);
    }

    @Test
    void removeMember_cannotRemoveCreator_throwsBadRequest() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));

        assertThatThrownBy(() -> boardService.removeMember(10L, CREATOR_ID, CREATOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("creator");
    }

    @Test
    void removeMember_notMember_throwsBadRequest() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.existsByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(false);

        assertThatThrownBy(() -> boardService.removeMember(10L, OTHER_ID, CREATOR_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a member");
    }

    // ─── getAllBoards ─────────────────────────────────────────────────────────

    @Test
    void getAllBoards_returnsList() {
        when(boardRepository.findAll()).thenReturn(List.of(publicBoard, privateBoard));

        List<BoardResponse> result = boardService.getAllBoards();

        assertThat(result).hasSize(2);
    }

    // ─── updateMemberRole ─────────────────────────────────────────────────────

    @Test
    void updateMemberRole_happyPath_succeeds() {
        UpdateBoardMemberRoleRequest req = new UpdateBoardMemberRoleRequest();
        req.setRole("ADMIN");

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.findByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(Optional.of(adminMember));
        when(memberRepository.save(any(BoardMember.class))).thenReturn(adminMember);

        BoardMemberResponse result = boardService.updateMemberRole(10L, OTHER_ID, req, CREATOR_ID);

        assertThat(result).isNotNull();
    }

    @Test
    void updateMemberRole_memberNotFound_throwsResourceNotFound() {
        UpdateBoardMemberRoleRequest req = new UpdateBoardMemberRoleRequest();
        req.setRole("MEMBER");

        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        // first call for assertAdminOrCreator (creator is ok), second call for
        // findMember
        when(memberRepository.findByBoard_BoardIdAndUserId(10L, OTHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.updateMemberRole(10L, OTHER_ID, req, CREATOR_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getMembers ───────────────────────────────────────────────────────────

    @Test
    void getMembers_publicBoard_returnsList() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(publicBoard));
        when(memberRepository.findByBoard_BoardId(10L)).thenReturn(List.of(adminMember));

        List<BoardMemberResponse> result = boardService.getMembers(10L, OTHER_ID);

        assertThat(result).hasSize(1);
    }
}
