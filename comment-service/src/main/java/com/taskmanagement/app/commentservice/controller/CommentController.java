package com.taskmanagement.app.commentservice.controller;

import com.taskmanagement.app.commentservice.dto.*;
import com.taskmanagement.app.commentservice.feign.AuthServiceClient;
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

import java.util.List;

@RestController
@Tag(name = "Comments & Attachments", description = "Threaded comments and file attachment APIs")
public class CommentController {

    @Autowired private CommentService commentService;
    @Autowired private JWTUtil jwtUtil;
    @Autowired private AuthServiceClient authServiceClient;
    @Autowired private HttpServletRequest httpServletRequest;

    @PostMapping("/comments")
    @Operation(summary = "Add a top-level comment or a reply to an existing comment")
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody AddCommentRequest req,
                                                      HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(req, userId(http) ,getToken()));
    }

    @GetMapping("/comments/card/{cardId}")
    @Operation(summary = "Get all top-level comments for a card (with replies nested)")
    public ResponseEntity<List<CommentResponse>> getByCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(commentService.getCommentsByCard(cardId,getToken()));
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
        return ResponseEntity.ok(commentService.getCommentCount(cardId,getToken()));
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
    @Operation(summary = "Add a file attachment to a card")
    public ResponseEntity<AttachmentResponse> addAttachment(@Valid @RequestBody AddAttachmentRequest req,
                                                            HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addAttachment(req, userId(http) , getToken()));
    }

    @GetMapping("/attachments/card/{cardId}")
    @Operation(summary = "Get all attachments for a card")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long cardId) {
        return ResponseEntity.ok(commentService.getAttachmentsByCard(cardId , getToken()));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete your own attachment")
    public ResponseEntity<String> deleteAttachment(@PathVariable Long attachmentId, HttpServletRequest http) {
        commentService.deleteAttachment(attachmentId, userId(http));
        return ResponseEntity.ok("Attachment deleted");
    }


    private Long userId(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new RuntimeException("Authorization header missing");
        return authServiceClient.getUserByUsername(jwtUtil.extractUsername(header.substring(7))).getBody().getUserId();
    }

    private String getToken() {
        String header = httpServletRequest.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header;
        }
        throw new RuntimeException("Token not found");
    }
}
