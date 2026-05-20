package com.taskmanagement.app.listservice.service;

import com.taskmanagement.app.listservice.dto.*;
import com.taskmanagement.app.listservice.entity.TaskList;
import com.taskmanagement.app.listservice.exception.BadRequestException;
import com.taskmanagement.app.listservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.listservice.feign.BoardServiceClient;
import com.taskmanagement.app.listservice.repository.ListRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListServiceImplTest {

    @Mock
    private ListRepository listRepository;
    @Mock
    private BoardServiceClient boardServiceClient;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ListServiceImpl listService;

    private TaskList sampleList;
    private BoardResponse openBoard;
    private BoardResponse closedBoard;
    private final Long BOARD_ID = 5L;
    private final Long LIST_ID = 1L;

    @BeforeEach
    void setUp() {
        sampleList = new TaskList();
        sampleList.setListId(LIST_ID);
        sampleList.setBoardId(BOARD_ID);
        sampleList.setName("To Do");
        sampleList.setPosition(0);
        sampleList.setArchived(false);

        openBoard = new BoardResponse();
        openBoard.setBoardId(BOARD_ID);
        openBoard.setName("Open Board");
        openBoard.setClosed(false);

        closedBoard = new BoardResponse();
        closedBoard.setBoardId(BOARD_ID);
        closedBoard.setName("Closed Board");
        closedBoard.setClosed(true);

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test.token");
    }

    // ─── createList ───────────────────────────────────────────────────────────

    @Test
    void createList_happyPath_returnsResponse() {
        CreateListRequest req = new CreateListRequest();
        req.setBoardId(BOARD_ID);
        req.setName("To Do");

        when(boardServiceClient.getBoardById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(openBoard));
        when(listRepository.findMaxPositionByBoardId(BOARD_ID)).thenReturn(Optional.empty());
        when(listRepository.save(any(TaskList.class))).thenReturn(sampleList);

        ListResponse result = listService.createList(req);

        assertThat(result).isNotNull();
        assertThat(result.getListId()).isEqualTo(LIST_ID);
        verify(listRepository).save(any(TaskList.class));
    }

    @Test
    void createList_boardNotFound_throwsBadRequest() {
        CreateListRequest req = new CreateListRequest();
        req.setBoardId(BOARD_ID);
        req.setName("To Do");

        when(boardServiceClient.getBoardById(BOARD_ID, "Bearer test.token"))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> listService.createList(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Board not found");
    }

    @Test
    void createList_closedBoard_throwsBadRequest() {
        CreateListRequest req = new CreateListRequest();
        req.setBoardId(BOARD_ID);
        req.setName("To Do");

        when(boardServiceClient.getBoardById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(closedBoard));

        assertThatThrownBy(() -> listService.createList(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("closed");
    }

    // ─── getListById ──────────────────────────────────────────────────────────

    @Test
    void getListById_found_returnsResponse() {
        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));

        ListResponse result = listService.getListById(LIST_ID);

        assertThat(result.getListId()).isEqualTo(LIST_ID);
    }

    @Test
    void getListById_notFound_throwsResourceNotFound() {
        when(listRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listService.getListById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getListsByBoard ──────────────────────────────────────────────────────

    @Test
    void getListsByBoard_returnsList() {
        when(listRepository.findByBoardIdAndIsArchived(BOARD_ID, false)).thenReturn(List.of(sampleList));

        List<ListResponse> results = listService.getListsByBoard(BOARD_ID);

        assertThat(results).hasSize(1);
    }

    // ─── getArchivedLists ─────────────────────────────────────────────────────

    @Test
    void getArchivedLists_returnsList() {
        sampleList.setArchived(true);
        when(listRepository.findByBoardIdAndIsArchived(BOARD_ID, true)).thenReturn(List.of(sampleList));

        List<ListResponse> results = listService.getArchivedLists(BOARD_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isArchived()).isTrue();
    }

    // ─── updateList ───────────────────────────────────────────────────────────

    @Test
    void updateList_notArchived_succeeds() {
        UpdateListRequest req = new UpdateListRequest();
        req.setName("In Progress");

        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(listRepository.save(any(TaskList.class))).thenReturn(sampleList);

        ListResponse result = listService.updateList(LIST_ID, req);

        assertThat(result).isNotNull();
        verify(listRepository).save(sampleList);
    }

    @Test
    void updateList_archivedList_throwsBadRequest() {
        sampleList.setArchived(true);
        UpdateListRequest req = new UpdateListRequest();

        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));

        assertThatThrownBy(() -> listService.updateList(LIST_ID, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("archived");
    }

    // ─── reorderLists ─────────────────────────────────────────────────────────

    @Test
    void reorderLists_validOrder_succeeds() {
        TaskList list2 = new TaskList();
        list2.setListId(2L);
        list2.setBoardId(BOARD_ID);
        list2.setPosition(1);

        ReorderListRequest req = new ReorderListRequest();
        req.setOrderedListIds(List.of(LIST_ID, 2L));

        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(listRepository.findById(2L)).thenReturn(Optional.of(list2));
        when(listRepository.save(any(TaskList.class))).thenReturn(sampleList);

        listService.reorderLists(BOARD_ID, req);

        verify(listRepository, times(2)).save(any(TaskList.class));
    }

    @Test
    void reorderLists_listBelongsToDifferentBoard_throwsBadRequest() {
        TaskList wrongList = new TaskList();
        wrongList.setListId(2L);
        wrongList.setBoardId(99L); // different board

        ReorderListRequest req = new ReorderListRequest();
        req.setOrderedListIds(List.of(2L));

        when(listRepository.findById(2L)).thenReturn(Optional.of(wrongList));

        assertThatThrownBy(() -> listService.reorderLists(BOARD_ID, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to board");
    }

    // ─── archiveList ──────────────────────────────────────────────────────────

    @Test
    void archiveList_succeeds() {
        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(listRepository.save(any(TaskList.class))).thenReturn(sampleList);

        ListResponse result = listService.archiveList(LIST_ID);

        assertThat(result).isNotNull();
        verify(listRepository).save(sampleList);
    }

    // ─── unarchiveList ────────────────────────────────────────────────────────

    @Test
    void unarchiveList_succeeds() {
        sampleList.setArchived(true);
        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(listRepository.save(any(TaskList.class))).thenReturn(sampleList);

        ListResponse result = listService.unarchiveList(LIST_ID);

        assertThat(result).isNotNull();
    }

    // ─── deleteList ───────────────────────────────────────────────────────────

    @Test
    void deleteList_archivedList_succeeds() {
        sampleList.setArchived(true);
        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));

        listService.deleteList(LIST_ID);

        verify(listRepository).delete(sampleList);
    }

    @Test
    void deleteList_notArchived_throwsBadRequest() {
        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));

        assertThatThrownBy(() -> listService.deleteList(LIST_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("archived");
    }

    // ─── moveList ─────────────────────────────────────────────────────────────

    @Test
    void moveList_happyPath_succeeds() {
        Long targetBoardId = 99L;
        BoardResponse targetBoard = new BoardResponse();
        targetBoard.setBoardId(targetBoardId);
        targetBoard.setWorkspaceId(1L);
        targetBoard.setClosed(false);

        openBoard.setWorkspaceId(1L); // same workspace

        MoveListRequest req = new MoveListRequest();
        req.setTargetBoardId(targetBoardId);
        req.setPosition(0);

        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(boardServiceClient.getBoardById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(openBoard));
        when(boardServiceClient.getBoardById(targetBoardId, "Bearer test.token"))
                .thenReturn(ResponseEntity.ok(targetBoard));
        when(listRepository.save(any(TaskList.class))).thenReturn(sampleList);

        ListResponse result = listService.moveList(LIST_ID, req);

        assertThat(result).isNotNull();
    }

    @Test
    void moveList_crossWorkspace_throwsBadRequest() {
        Long targetBoardId = 99L;
        BoardResponse targetBoard = new BoardResponse();
        targetBoard.setBoardId(targetBoardId);
        targetBoard.setWorkspaceId(2L); // different workspace

        openBoard.setWorkspaceId(1L);

        MoveListRequest req = new MoveListRequest();
        req.setTargetBoardId(targetBoardId);

        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(boardServiceClient.getBoardById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(openBoard));
        when(boardServiceClient.getBoardById(targetBoardId, "Bearer test.token"))
                .thenReturn(ResponseEntity.ok(targetBoard));

        assertThatThrownBy(() -> listService.moveList(LIST_ID, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot move list across workspaces");
    }

    @Test
    void moveList_toClosedBoard_throwsBadRequest() {
        Long targetBoardId = 99L;
        BoardResponse targetBoard = new BoardResponse();
        targetBoard.setBoardId(targetBoardId);
        targetBoard.setWorkspaceId(1L);
        targetBoard.setClosed(true);

        openBoard.setWorkspaceId(1L);

        MoveListRequest req = new MoveListRequest();
        req.setTargetBoardId(targetBoardId);

        when(listRepository.findById(LIST_ID)).thenReturn(Optional.of(sampleList));
        when(boardServiceClient.getBoardById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(openBoard));
        when(boardServiceClient.getBoardById(targetBoardId, "Bearer test.token"))
                .thenReturn(ResponseEntity.ok(targetBoard));

        assertThatThrownBy(() -> listService.moveList(LIST_ID, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("closed");
    }
}
