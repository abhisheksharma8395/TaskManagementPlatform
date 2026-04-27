package com.taskmanagement.app.listservice.service;

import com.taskmanagement.app.listservice.dto.*;
import com.taskmanagement.app.listservice.entity.TaskList;
import com.taskmanagement.app.listservice.exception.BadRequestException;
import com.taskmanagement.app.listservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.listservice.feign.BoardServiceClient;
import com.taskmanagement.app.listservice.repository.ListRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private ListRepository listRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private BoardServiceClient boardServiceClient;

    @Override
    @Transactional
    public ListResponse createList(CreateListRequest request) {
        String token = httpServletRequest.getHeader("Authorization");

        // checking if board with board id exists or not.
        BoardResponse board;
        try {
            board = boardServiceClient.getBoardById(request.getBoardId(), token);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("Board not found with id: " + request.getBoardId());
        } catch (FeignException e) {
            throw new BadRequestException("Could not verify board: " + e.getMessage());
        }

        // if board is closed then no new lists allowed in that
        if (board.isClosed()) {
            throw new BadRequestException("Cannot add a list to a closed board");
        }

        // Checking the maximum position of list using boardId
        int nextPosition = listRepository.findMaxPositionByBoardId(request.getBoardId()).map(max -> max + 1).orElse(0);
        TaskList list = new TaskList();
        list.setBoardId(request.getBoardId());
        list.setName(request.getName());
        list.setPosition(nextPosition);
        list.setColor(request.getColor());
        return toResponse(listRepository.save(list));
    }

    @Override
    public ListResponse getListById(Long listId) {
        return toResponse(findOrThrow(listId));
    }

    @Override
    public List<ListResponse> getListsByBoard(Long boardId) {
        return listRepository.findByBoardIdAndIsArchived(boardId, false)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ListResponse> getArchivedLists(Long boardId) {
        return listRepository.findByBoardIdAndIsArchived(boardId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ListResponse updateList(Long listId, UpdateListRequest request) {
        TaskList list = findOrThrow(listId);
        if (list.isArchived()) throw new BadRequestException("Cannot update an archived list");
        if (request.getName() != null && !request.getName().isBlank()) list.setName(request.getName());
        if (request.getColor() != null) list.setColor(request.getColor());
        return toResponse(listRepository.save(list));
    }

    @Override
    @Transactional
    public void reorderLists(Long boardId, ReorderListRequest request) {
        List<Long> ids = request.getOrderedListIds();
        for (int i = 0; i < ids.size(); i++) {
            TaskList list = findOrThrow(ids.get(i));
            if (!list.getBoardId().equals(boardId))
                throw new BadRequestException("List " + ids.get(i) + " does not belong to board " + boardId);
            list.setPosition(i);
            listRepository.save(list);
        }
    }

    @Override
    @Transactional
    public ListResponse archiveList(Long listId) {
        TaskList list = findOrThrow(listId);
        list.setArchived(true);
        return toResponse(listRepository.save(list));
    }

    @Override
    @Transactional
    public ListResponse unarchiveList(Long listId) {
        TaskList list = findOrThrow(listId);
        list.setArchived(false);
        return toResponse(listRepository.save(list));
    }

    @Override
    @Transactional
    public void deleteList(Long listId) {
        TaskList list = findOrThrow(listId);
        if (!list.isArchived()) throw new BadRequestException("Only archived lists can be permanently deleted");
        listRepository.delete(list);
    }

    @Override
    @Transactional
    public ListResponse moveList(Long listId, MoveListRequest request) {
        String token = httpServletRequest.getHeader("Authorization");
        TaskList list = findOrThrow(listId);

        // 1. Fetch source board to get its workspaceId
        BoardResponse sourceBoard;
        try {
            sourceBoard = boardServiceClient.getBoardById(list.getBoardId(), token);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("Source board not found with id: " + list.getBoardId());
        } catch (FeignException e) {
            throw new BadRequestException("Could not verify source board: " + e.getMessage());
        }

        // 2. Fetch target board to validate it exists and is not closed
        BoardResponse targetBoard;
        try {
            targetBoard = boardServiceClient.getBoardById(request.getTargetBoardId(), token);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("Target board not found with id: " + request.getTargetBoardId());
        } catch (FeignException e) {
            throw new BadRequestException("Could not verify target board: " + e.getMessage());
        }

        // 3. Enforce same-workspace constraint (case study: "within the same workspace")
        if (!sourceBoard.getWorkspaceId().equals(targetBoard.getWorkspaceId())) {
            throw new BadRequestException(
                    "Cannot move list across workspaces. Source board is in workspace "
                            + sourceBoard.getWorkspaceId()
                            + ", target board is in workspace "
                            + targetBoard.getWorkspaceId()
            );
        }

        // 4. Reject move to a closed board
        if (targetBoard.isClosed()) {
            throw new BadRequestException("Cannot move a list to a closed board");
        }

        // 5. Perform the move
        list.setBoardId(request.getTargetBoardId());
        if (request.getPosition() != null) {
            list.setPosition(request.getPosition());
        } else {
            int nextPos = listRepository.findMaxPositionByBoardId(request.getTargetBoardId())
                    .map(m -> m + 1).orElse(0);
            list.setPosition(nextPos);
        }
        return toResponse(listRepository.save(list));
    }

    private TaskList findOrThrow(Long listId) {
        return listRepository.findById(listId).orElseThrow(() -> new ResourceNotFoundException("List not found with id: " + listId));
    }

    private ListResponse toResponse(TaskList l) {
        ListResponse r = new ListResponse();
        r.setListId(l.getListId());
        r.setBoardId(l.getBoardId());
        r.setName(l.getName());
        r.setPosition(l.getPosition());
        r.setColor(l.getColor());
        r.setArchived(l.isArchived());
        r.setCreatedAt(l.getCreatedAt());
        r.setUpdatedAt(l.getUpdatedAt());
        return r;
    }
}
