package com.taskmanagement.app.commentservice.controller;

import com.taskmanagement.app.commentservice.dto.*;
import com.taskmanagement.app.commentservice.service.CommentService;
import com.taskmanagement.app.commentservice.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Comments & Attachments", description = "Threaded comments and file attachment APIs")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @PostMapping("/comments")
    @Operation(summary = "Add a top-level comment or a reply to an existing comment")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody AddCommentRequest req,
            HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(req, userId(http), getToken()));
    }

    @GetMapping("/comments/card/{cardId}")
    @Operation(summary = "Get all top-level comments for a card (with replies nested)")
    public ResponseEntity<List<CommentResponse>> getByCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(commentService.getCommentsByCard(cardId, getToken()));
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "Get a single comment by ID")
    public ResponseEntity<CommentResponse> getById(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "Get all replies to a comment")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    @GetMapping("/comments/card/{cardId}/count")
    @Operation(summary = "Get the total comment count for a card")
    public ResponseEntity<Long> getCount(@PathVariable Long cardId) {
        return ResponseEntity.ok(commentService.getCommentCount(cardId, getToken()));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Edit your own comment")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest req,
            HttpServletRequest http) {
        return ResponseEntity.ok(commentService.updateComment(commentId, req, userId(http)));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Soft-delete your own comment")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, HttpServletRequest http) {
        commentService.deleteComment(commentId, userId(http));
        return ResponseEntity.ok("Comment deleted");
    }

    @PostMapping("/attachments")
    public ResponseEntity<AttachmentResponse> addAttachment(
            @ModelAttribute @Valid AddAttachmentRequest request,
            HttpServletRequest httpServletRequest) throws IOException {

        Long uploaderId = userId(httpServletRequest);
        String token = httpServletRequest.getHeader("Authorization");
        AttachmentResponse response = commentService.addAttachment(request, uploaderId, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/attachments/card/{cardId}")
    @Operation(summary = "Get all attachments for a card")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long cardId) {
        return ResponseEntity.ok(commentService.getAttachmentsByCard(cardId, getToken()));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId,
            HttpServletRequest httpServletRequest) throws IOException {

        Long requesterId = userId(httpServletRequest);
        commentService.deleteAttachment(attachmentId, requesterId);
        return ResponseEntity.noContent().build();
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

    private String getToken() {
        String header = httpServletRequest.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header;
        }
        return null;
    }
}
