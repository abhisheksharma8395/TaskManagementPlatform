package com.taskmanagement.app.boardservice.controller;

import com.taskmanagement.app.boardservice.dto.*;
import com.taskmanagement.app.boardservice.feign.AuthServiceClient;
import com.taskmanagement.app.boardservice.service.BoardService;
import com.taskmanagement.app.boardservice.util.JWTUtil;
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
@RequestMapping("/boards")
@Tag(name = "Board", description = "Board and BoardMember management APIs")
public class BoardController {

    @Autowired private BoardService boardService;
    @Autowired private JWTUtil jwtUtil;
    @Autowired private AuthServiceClient authServiceClient;

    @PostMapping
    @Operation(summary = "Create a new board")
    public ResponseEntity<BoardResponse> create(@Valid @RequestBody CreateBoardRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(req, extractUserId(http)));
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "Get board by ID")
    public ResponseEntity<BoardResponse> getById(@PathVariable Long boardId, HttpServletRequest http) {
        return ResponseEntity.ok(boardService.getBoardById(boardId, extractUserId(http)));
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get all boards in a workspace")
    public ResponseEntity<List<BoardResponse>> getByWorkspace(@PathVariable Long workspaceId, HttpServletRequest http) {
        return ResponseEntity.ok(boardService.getBoardsByWorkspace(workspaceId, extractUserId(http)));
    }

    @GetMapping("/member/{userId}")
    @Operation(summary = "Get all boards a user is a member of")
    public ResponseEntity<List<BoardResponse>> getByMember(@PathVariable Long userId) {
        return ResponseEntity.ok(boardService.getBoardsByMember(userId));
    }

    @PutMapping("/{boardId}")
    @Operation(summary = "Update board details")
    public ResponseEntity<BoardResponse> update(@PathVariable Long boardId, @Valid @RequestBody UpdateBoardRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(boardService.updateBoard(boardId, req, extractUserId(http)));
    }

    @PutMapping("/{boardId}/close")
    @Operation(summary = "Close a board")
    public ResponseEntity<BoardResponse> close(@PathVariable Long boardId, HttpServletRequest http) {
        return ResponseEntity.ok(boardService.closeBoard(boardId, extractUserId(http)));
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "Delete a board")
    public ResponseEntity<String> delete(@PathVariable Long boardId, HttpServletRequest http) {
        boardService.deleteBoard(boardId, extractUserId(http));
        return ResponseEntity.ok("Board deleted successfully");
    }

    @PostMapping("/{boardId}/members")
    @Operation(summary = "Add a member to a board")
    public ResponseEntity<BoardMemberResponse> addMember(@PathVariable Long boardId, @Valid @RequestBody AddBoardMemberRequest req, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.addMember(boardId, req, extractUserId(http)));
    }

    @DeleteMapping("/{boardId}/members/{userId}")
    @Operation(summary = "Remove a member from a board")
    public ResponseEntity<String> removeMember(@PathVariable Long boardId, @PathVariable Long userId, HttpServletRequest http) {
        boardService.removeMember(boardId, userId, extractUserId(http));
        return ResponseEntity.ok("Member removed");
    }

    @PutMapping("/{boardId}/members/{userId}/role")
    @Operation(summary = "Update a member's role")
    public ResponseEntity<BoardMemberResponse> updateRole(@PathVariable Long boardId, @PathVariable Long userId, @Valid @RequestBody UpdateBoardMemberRoleRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(boardService.updateMemberRole(boardId, userId, req, extractUserId(http)));
    }

    @GetMapping("/{boardId}/members")
    @Operation(summary = "List all board members")
    public ResponseEntity<List<BoardMemberResponse>> getMembers(@PathVariable Long boardId, HttpServletRequest http) {
        return ResponseEntity.ok(boardService.getMembers(boardId, extractUserId(http)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing or malformed");
        }
        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);  // username IS in the token
        return authServiceClient.getUserByUsername(username).getBody().getUserId();  // fetch userId from auth-service
    }
}
