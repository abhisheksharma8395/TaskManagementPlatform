package com.taskmanagement.app.authservice.service;

import com.taskmanagement.app.authservice.dto.AuthResponse;
import com.taskmanagement.app.authservice.dto.RegisterRequest;
import com.taskmanagement.app.authservice.dto.ResetPasswordRequest;
import com.taskmanagement.app.authservice.dto.UserProfileResponse;
import com.taskmanagement.app.authservice.exception.InvalidUserOperationException;
import com.taskmanagement.app.authservice.exception.InvalidUserRegisterException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AuthService {
    UserProfileResponse register(RegisterRequest request) throws InvalidUserRegisterException;
    boolean validatingUserRegister(RegisterRequest request) throws InvalidUserRegisterException;
    AuthResponse login(String username, String password);
    void logout(String username);
    String refreshToken(String token);
    UserProfileResponse getUserByEmail(String email);
    UserProfileResponse getUserById(Long id);
    UserProfileResponse updateProfile(Long id, RegisterRequest request);
    String changePassword(String username, String oldPassword, String newPassword) throws InvalidUserOperationException;
    String deactivateAccount(Long id) throws InvalidUserOperationException;
    List<UserProfileResponse> searchUsersByFullName(String fullName);
    List<UserProfileResponse> searchUsersByRole(String role);
    UserProfileResponse getUserByUsername(String username);
    String uploadAvatar(MultipartFile file, HttpServletRequest request) throws IOException;
    void forgotPassword(String email);
    String resetPassword(ResetPasswordRequest request);
}