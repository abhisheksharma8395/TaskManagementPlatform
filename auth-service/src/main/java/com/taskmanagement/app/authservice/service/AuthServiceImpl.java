package com.taskmanagement.app.authservice.service;

import com.taskmanagement.app.authservice.dto.UserRegisterResponse;
import com.taskmanagement.app.authservice.entity.User;
import com.taskmanagement.app.authservice.exception.InvalidUserRegisterException;
import com.taskmanagement.app.authservice.repository.UserRepository;
import com.taskmanagement.app.authservice.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    JWTUtil jwtUtil;

    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    public User register(UserRegisterResponse userResponse) {
        try{
            boolean isValidRegister = validatingUserRegister(userResponse);
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
            User user = new User();
            user.setFullName(userResponse.getFullName());
            user.setUsername(userResponse.getUserName());
            user.setEmail(userResponse.getEmail());
            user.setPasswordHash(passwordEncoder.encode(userResponse.getPassword()));
            user.setRole(userResponse.getRole());
            userRepository.save(user);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validatingUserRegister(UserRegisterResponse userResponse) throws InvalidUserRegisterException {
        if (userResponse == null) return false;
        String fullNameRegex = "^[A-Za-z ]{3,50}$";
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        String usernameRegex = "^[A-Za-z0-9_]{4,20}$";
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
        String roleRegex = "^(USER|ADMIN)$";
        if (userResponse.getFullName() == null || !Pattern.matches(fullNameRegex, userResponse.getFullName()))
            throw new InvalidUserRegisterException("Invalid full name");

        if (userResponse.getEmail() == null || !Pattern.matches(emailRegex, userResponse.getEmail()))
            throw new InvalidUserRegisterException("Invalid email it should be in format like eg. abc@gmail.com");

        if (userResponse.getUserName() == null || !Pattern.matches(usernameRegex, userResponse.getUserName()))
            throw new InvalidUserRegisterException("Invalid username");

        if (userResponse.getPassword() == null || !Pattern.matches(passwordRegex, userResponse.getPassword()))
            throw new InvalidUserRegisterException("Invalid password! password should contain at-least one uppercase letter, one lowercase, one digit and one special character and length in between 8 to 20");

        if (userResponse.getRole() == null || !Pattern.matches(roleRegex, userResponse.getRole().toUpperCase()))
            throw new InvalidUserRegisterException("Invalid Role");

        return true;
    }

    @Override
    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            if (authentication.isAuthenticated()) {
                return jwtUtil.generateToken(username);
            }
            else{
                return "Login Failed! Check Credentials.";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logout(String email) {

    }

    @Override
    public boolean validateToken(String token) {
        return true;
    }

    @Override
    public String refreshToken(String token) {
        return "";
    }

    @Override
    public User getUserByEmail(String email) {
        return null;
    }

    @Override
    public User getUserById(Integer id) {
        return null;
    }

    @Override
    public User updateProfile(Integer id, User user) {
        return null;
    }

    @Override
    public void changePassword(Integer id, String password) {

    }

    @Override
    public void deactivateAccount(Integer id) {

    }

    @Override
    public List<User> searchUsersByFullName(String fullName) {
        return List.of();
    }

    @Override
    public List<User> searchUsersByRole(String role) {
        return List.of();
    }
}
