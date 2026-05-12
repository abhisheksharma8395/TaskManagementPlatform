package com.taskmanagement.app.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth-service")
    public ResponseEntity<Map<String, String>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Auth service is currently unavailable. Please try again later."
                ));
    }

    @RequestMapping("/workspace-service")
    public ResponseEntity<Map<String, String>> workspaceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Workspace service is currently unavailable."
                ));
    }

    @RequestMapping("/board-service")
    public ResponseEntity<Map<String, String>> boardFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Board service is currently unavailable."
                ));
    }

    @RequestMapping("/card-service")
    public ResponseEntity<Map<String, String>> cardFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Card service is currently unavailable."
                ));
    }

    @RequestMapping("/list-service")
    public ResponseEntity<Map<String, String>> listFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "List service is currently unavailable."
                ));
    }

    @RequestMapping("/comment-service")
    public ResponseEntity<Map<String, String>> commentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Comment service is currently unavailable."
                ));
    }

    @RequestMapping("/label-service")
    public ResponseEntity<Map<String, String>> labelFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Label service is currently unavailable."
                ));
    }

    @RequestMapping("/notification-service")
    public ResponseEntity<Map<String, String>> notificationFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "message", "Notification service is currently unavailable."
                ));
    }
}