package com.taskmanagement.app.listservice.service;

import com.taskmanagement.app.listservice.dto.*;

import java.util.List;

public interface ListService {
    ListResponse createList(CreateListRequest request);
    ListResponse getListById(Long listId);
    List<ListResponse> getListsByBoard(Long boardId);
    List<ListResponse> getArchivedLists(Long boardId);
    ListResponse updateList(Long listId, UpdateListRequest request);
    void reorderLists(Long boardId, ReorderListRequest request);
    ListResponse archiveList(Long listId);
    ListResponse unarchiveList(Long listId);
    void deleteList(Long listId);
    ListResponse moveList(Long listId, MoveListRequest request);
}
