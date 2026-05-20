package com.taskmanagement.app.labelservice.controller;

import com.taskmanagement.app.labelservice.dto.*;
import com.taskmanagement.app.labelservice.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Labels & Checklists", description = "Board label and card checklist management APIs")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @PostMapping("/labels")
    @Operation(summary = "Create a new label for a board")
    public ResponseEntity<LabelResponse> createLabel(@Valid @RequestBody CreateLabelRequest req, @RequestHeader("Authorization") String token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.createLabel(req, token));
    }

    @GetMapping("/labels/board/{boardId}")
    @Operation(summary = "Get all labels for a board")
    public ResponseEntity<List<LabelResponse>> getLabelsByBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(labelService.getLabelsByBoard(boardId));
    }

    @GetMapping("/labels/{labelId}")
    @Operation(summary = "Get a label by ID")
    public ResponseEntity<LabelResponse> getLabelById(@PathVariable Long labelId) {
        return ResponseEntity.ok(labelService.getLabelById(labelId));
    }

    @PutMapping("/labels/{labelId}")
    @Operation(summary = "Update label name or colour")
    public ResponseEntity<LabelResponse> updateLabel(@PathVariable Long labelId, @Valid @RequestBody UpdateLabelRequest req) {
        return ResponseEntity.ok(labelService.updateLabel(labelId, req));
    }

    @DeleteMapping("/labels/{labelId}")
    @Operation(summary = "Delete a label and remove it from all cards")
    public ResponseEntity<String> deleteLabel(@PathVariable Long labelId) {
        labelService.deleteLabel(labelId);
        return ResponseEntity.ok("Label deleted");
    }

    @PostMapping("/labels/card")
    @Operation(summary = "Attach a label to a card")
    public ResponseEntity<String> addLabelToCard(@Valid @RequestBody CardLabelRequest req, @RequestHeader("Authorization") String token) {
        labelService.addLabelToCard(req, token);
        return ResponseEntity.status(HttpStatus.CREATED).body("Label attached to card");
    }

    @DeleteMapping("/labels/card/{cardId}/label/{labelId}")
    @Operation(summary = "Remove a label from a card")
    public ResponseEntity<String> removeLabelFromCard(@PathVariable Long cardId, @PathVariable Long labelId) {
        labelService.removeLabelFromCard(cardId, labelId);
        return ResponseEntity.ok("Label removed from card");
    }

    @GetMapping("/labels/card/{cardId}")
    @Operation(summary = "Get all labels attached to a card")
    public ResponseEntity<List<LabelResponse>> getLabelsForCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(labelService.getLabelsForCard(cardId));
    }


    @PostMapping("/checklists")
    @Operation(summary = "Create a checklist on a card")
    public ResponseEntity<ChecklistResponse> createChecklist(@Valid @RequestBody CreateChecklistRequest req, @RequestHeader("Authorization") String token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.createChecklist(req, token));
    }

    @GetMapping("/checklists/{checklistId}")
    @Operation(summary = "Get a checklist by ID with all items")
    public ResponseEntity<ChecklistResponse> getChecklist(@PathVariable Long checklistId) {
        return ResponseEntity.ok(labelService.getChecklistById(checklistId));
    }

    @GetMapping("/checklists/card/{cardId}")
    @Operation(summary = "Get all checklists for a card")
    public ResponseEntity<List<ChecklistResponse>> getByCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(labelService.getChecklistsByCard(cardId));
    }

    @DeleteMapping("/checklists/{checklistId}")
    @Operation(summary = "Delete a checklist and all its items")
    public ResponseEntity<String> deleteChecklist(@PathVariable Long checklistId) {
        labelService.deleteChecklist(checklistId);
        return ResponseEntity.ok("Checklist deleted");
    }

    @GetMapping("/checklists/{checklistId}/progress")
    @Operation(summary = "Get completion percentage of a checklist (0-100)")
    public ResponseEntity<Integer> getProgress(@PathVariable Long checklistId) {
        return ResponseEntity.ok(labelService.getChecklistProgress(checklistId));
    }

    // ── Checklist Items ───────────────────────────────────────────────────

    @PostMapping("/checklists/{checklistId}/items")
    @Operation(summary = "Add an item to a checklist")
    public ResponseEntity<ChecklistItemResponse> addItem(@PathVariable Long checklistId, @Valid @RequestBody AddChecklistItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.addItem(checklistId, req));
    }

    @PutMapping("/checklists/items/{itemId}/toggle")
    @Operation(summary = "Toggle a checklist item complete / incomplete")
    public ResponseEntity<ChecklistItemResponse> toggleItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(labelService.toggleItem(itemId));
    }

    @DeleteMapping("/checklists/items/{itemId}")
    @Operation(summary = "Delete a checklist item")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        labelService.deleteItem(itemId);
        return ResponseEntity.ok("Item deleted");
    }
}
