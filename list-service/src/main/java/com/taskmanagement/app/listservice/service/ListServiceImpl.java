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
            board = boardServiceClient.getBoardById(request.getBoardId(), token).getBody();
            if (board == null) throw new BadRequestException("Board not found with id: " + request.getBoardId());
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
