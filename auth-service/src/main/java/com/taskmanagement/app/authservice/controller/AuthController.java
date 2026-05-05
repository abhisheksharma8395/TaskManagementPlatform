package com.taskmanagement.app.authservice.controller;

import com.taskmanagement.app.authservice.dto.*;
import com.taskmanagement.app.authservice.exception.InvalidUserOperationException;
import com.taskmanagement.app.authservice.service.AuthService;
import com.taskmanagement.app.authservice.service.CloudinaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody RegisterRequest registerRequest) throws Exception {
        UserProfileResponse profile = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        String avatarUrl = authService.uploadAvatar(file,request).getBody();
        return ResponseEntity.ok(avatarUrl);
    }

    @GetMapping("/user/id/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @GetMapping("/user/username/{username}")
    public ResponseEntity<UserProfileResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(authService.getUserByUsername(username));
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<UserProfileResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(authService.getUserByEmail(email));
    }

    @GetMapping("update/password")
    public ResponseEntity<String> changePassword(@RequestParam ChangePasswordRequest userRequest) throws InvalidUserOperationException {
        return ResponseEntity.ok(authService.changePassword(userRequest.getUsername(),userRequest.getOldPassword(), userRequest.getNewPassword()));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivateAccount(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(authService.deactivateAccount(id));
    }

    @GetMapping("/search/name")
    public ResponseEntity<?> searchByName(@RequestParam String fullName) {
        return ResponseEntity.ok(authService.searchUsersByFullName(fullName));
    }

    @GetMapping("/search/role/{role}")
    public ResponseEntity<?> searchByRole(@PathVariable String role) {
        return ResponseEntity.ok(authService.searchUsersByRole(role));
    }
}
