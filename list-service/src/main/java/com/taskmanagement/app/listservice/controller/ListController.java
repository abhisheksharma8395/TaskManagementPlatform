package com.taskmanagement.app.listservice.controller;

import com.taskmanagement.app.listservice.dto.*;
import com.taskmanagement.app.listservice.service.ListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
@Tag(name = "List", description = "Kanban list / column management APIs")
public class ListController {

    @Autowired private ListService listService;

    @PostMapping
    @Operation(summary = "Create a new list on a board")
    public ResponseEntity<ListResponse> create(@Valid @RequestBody CreateListRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listService.createList(req));
    }

    @GetMapping("/{listId}")
    @Operation(summary = "Get a list by ID")
    public ResponseEntity<ListResponse> getById(@PathVariable Long listId) {
        return ResponseEntity.ok(listService.getListById(listId));
    }

    @GetMapping("/board/{boardId}")
    @Operation(summary = "Get all active lists on a board (ordered by position)")
    public ResponseEntity<List<ListResponse>> getByBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(listService.getListsByBoard(boardId));
    }

    @GetMapping("/board/{boardId}/archived")
    @Operation(summary = "Get all archived lists on a board")
    public ResponseEntity<List<ListResponse>> getArchived(@PathVariable Long boardId) {
        return ResponseEntity.ok(listService.getArchivedLists(boardId));
    }

    @PutMapping("/{listId}")
    @Operation(summary = "Update list name or colour")
    public ResponseEntity<ListResponse> update(@PathVariable Long listId,
                                               @Valid @RequestBody UpdateListRequest req) {
        return ResponseEntity.ok(listService.updateList(listId, req));
    }

    @PutMapping("/board/{boardId}/reorder")
    @Operation(summary = "Reorder lists on a board via drag-and-drop")
    public ResponseEntity<String> reorder(@PathVariable Long boardId,
                                          @Valid @RequestBody ReorderListRequest req) {
        listService.reorderLists(boardId, req);
        return ResponseEntity.ok("Lists reordered successfully");
    }

    @PostMapping("/{listId}/archive")
    @Operation(summary = "Archive a list (soft delete)")
    public ResponseEntity<ListResponse> archive(@PathVariable Long listId) {
        return ResponseEntity.ok(listService.archiveList(listId));
    }

    @PostMapping("/{listId}/unarchive")
    @Operation(summary = "Restore an archived list")
    public ResponseEntity<ListResponse> unarchive(@PathVariable Long listId) {
        return ResponseEntity.ok(listService.unarchiveList(listId));
    }

    @DeleteMapping("/{listId}")
    @Operation(summary = "Permanently delete an archived list")
    public ResponseEntity<String> delete(@PathVariable Long listId) {
        listService.deleteList(listId);
        return ResponseEntity.ok("List permanently deleted");
    }

}
