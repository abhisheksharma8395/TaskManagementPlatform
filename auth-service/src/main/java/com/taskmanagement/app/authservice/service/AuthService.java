package com.taskmanagement.app.authservice.service;

import com.taskmanagement.app.authservice.dto.UserRegisterResponse;
import com.taskmanagement.app.authservice.entity.User;
import com.taskmanagement.app.authservice.exception.InvalidUserRegisterException;
import org.springframework.stereotype.Service;

import java.util.List;


public interface AuthService {
    User register(UserRegisterResponse user);
    boolean validatingUserRegister(UserRegisterResponse user) throws InvalidUserRegisterException;
    String login(String username , String password);
    void logout(String email);
    boolean validateToken(String token);
    String refreshToken(String token);
    User getUserByEmail(String email);
    User getUserById(Integer id);
    User updateProfile(Integer id, User user);
    void changePassword(Integer id , String password);
    void deactivateAccount(Integer id);
    List<User> searchUsersByFullName(String fullName);
    List<User> searchUsersByRole(String role);
}
