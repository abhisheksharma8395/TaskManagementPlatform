package com.taskmanagement.app.cardservice.controller;

import com.taskmanagement.app.cardservice.dto.*;
import com.taskmanagement.app.cardservice.service.CardService;
import com.taskmanagement.app.cardservice.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@Tag(name = "Card", description = "Card / Task management APIs")
public class CardController {

    @Autowired private CardService cardService;
    @Autowired private JWTUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Create a new card in a list")
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(req, userId(http)));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Get a card by ID")
    public ResponseEntity<CardResponse> getById(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    @GetMapping("/list/{listId}")
    @Operation(summary = "Get all active cards in a list (ordered by position)")
    public ResponseEntity<List<CardResponse>> getByList(@PathVariable Long listId) {
        return ResponseEntity.ok(cardService.getCardsByList(listId));
    }

    @GetMapping("/board/{boardId}")
    @Operation(summary = "Get all active cards on a board")
    public ResponseEntity<List<CardResponse>> getByBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(cardService.getCardsByBoard(boardId));
    }

    @GetMapping("/assignee/{userId}")
    @Operation(summary = "Get all cards assigned to a user")
    public ResponseEntity<List<CardResponse>> getByAssignee(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByAssignee(userId));
    }

    @GetMapping("/board/{boardId}/archived")
    @Operation(summary = "Get all archived cards on a board")
    public ResponseEntity<List<CardResponse>> getArchived(@PathVariable Long boardId) {
        return ResponseEntity.ok(cardService.getArchivedCards(boardId));
    }

    @PutMapping("/{cardId}")
    @Operation(summary = "Update card details")
    public ResponseEntity<CardResponse> update(@PathVariable Long cardId, @Valid @RequestBody UpdateCardRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(cardService.updateCard(cardId, req, userId(http)));
    }

    @PutMapping("/{cardId}/move")
    @Operation(summary = "Move a card to a different list (drag-and-drop)")
    public ResponseEntity<CardResponse> move(@PathVariable Long cardId, @Valid @RequestBody MoveCardRequest req) {
        return ResponseEntity.ok(cardService.moveCard(cardId, req));
    }

    @PutMapping("/list/{listId}/reorder")
    @Operation(summary = "Reorder cards within a list (drag-and-drop)")
    public ResponseEntity<String> reorder(@PathVariable Long listId, @Valid @RequestBody ReorderCardsRequest req) {
        cardService.reorderCards(listId, req);
        return ResponseEntity.ok("Cards reordered successfully");
    }

    @PutMapping("/{cardId}/assignee")
    @Operation(summary = "Assign or unassign a card (pass null assigneeId to unassign)")
    public ResponseEntity<CardResponse> setAssignee(@PathVariable Long cardId,
                                                    @RequestBody SetAssigneeRequest req) {
        return ResponseEntity.ok(cardService.setAssignee(cardId, req));
    }

    @PutMapping("/{cardId}/priority")
    @Operation(summary = "Set card priority: LOW / MEDIUM / HIGH / CRITICAL")
    public ResponseEntity<CardResponse> setPriority(@PathVariable Long cardId,
                                                    @Valid @RequestBody SetPriorityRequest req) {
        return ResponseEntity.ok(cardService.setPriority(cardId, req));
    }

    @PutMapping("/{cardId}/status")
    @Operation(summary = "Set card status: TO_DO / IN_PROGRESS / IN_REVIEW / DONE")
    public ResponseEntity<CardResponse> setStatus(@PathVariable Long cardId,
                                                  @Valid @RequestBody SetStatusRequest req) {
        return ResponseEntity.ok(cardService.setStatus(cardId, req));
    }

    @PostMapping("/{cardId}/archive")
    @Operation(summary = "Archive a card (soft delete)")
    public ResponseEntity<CardResponse> archive(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.archiveCard(cardId));
    }

    @PostMapping("/{cardId}/unarchive")
    @Operation(summary = "Restore an archived card")
    public ResponseEntity<CardResponse> unarchive(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.unarchiveCard(cardId));
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Permanently delete an archived card")
    public ResponseEntity<String> delete(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card permanently deleted");
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue cards across the platform (Platform Admin)")
    public ResponseEntity<List<CardResponse>> getAllOverdue() {
        return ResponseEntity.ok(cardService.getOverdueCards());
    }

    @GetMapping("/board/{boardId}/overdue")
    @Operation(summary = "Get overdue cards for a specific board")
    public ResponseEntity<List<CardResponse>> getOverdueByBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(cardService.getOverdueCardsByBoard(boardId));
    }


    private Long userId(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Authorization header missing");
        Long userId = jwtUtil.extractUserId(header.substring(7));
        if (userId == null)
            throw new RuntimeException("userId claim missing in JWT token");
        return userId;
    }
}
