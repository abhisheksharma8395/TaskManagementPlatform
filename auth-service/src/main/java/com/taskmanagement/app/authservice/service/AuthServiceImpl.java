package com.taskmanagement.app.authservice.service;

import com.taskmanagement.app.authservice.dto.AuthResponse;
import com.taskmanagement.app.authservice.dto.RegisterRequest;
import com.taskmanagement.app.authservice.dto.UserProfileResponse;
import com.taskmanagement.app.authservice.entity.User;
import com.taskmanagement.app.authservice.exception.InvalidUserOperationException;
import com.taskmanagement.app.authservice.exception.InvalidUserRegisterException;
import com.taskmanagement.app.authservice.repository.UserRepository;
import com.taskmanagement.app.authservice.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    JWTUtil jwtUtil;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    CloudinaryService cloudinaryService;

    @Override
    public UserProfileResponse register(RegisterRequest request) throws InvalidUserRegisterException {
        validatingUserRegister(request);

        // checking for duplicate email and username before saving the user in database
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidUserRegisterException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUserName())) {
            throw new InvalidUserRegisterException("Username is already taken");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // fixed: uses the @Bean
        user.setRole(request.getRole().toUpperCase());
        user.setActive(true);
        userRepository.save(user);
        return mapToProfile(user);
    }

    // This Method is checking when user register the data is valid or not
    @Override
    public boolean validatingUserRegister(RegisterRequest request) throws InvalidUserRegisterException {
        if (request == null) throw new InvalidUserRegisterException("Request body cannot be null");

        String fullNameRegex = "^[A-Za-z ]{3,50}$";
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        String usernameRegex = "^[A-Za-z0-9_]{4,20}$";
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
        String roleRegex = "^(USER|ADMIN)$";

        if (request.getFullName() == null || !Pattern.matches(fullNameRegex, request.getFullName()))
            throw new InvalidUserRegisterException("Invalid full name. Use 3-50 letters only.");

        if (request.getEmail() == null || !Pattern.matches(emailRegex, request.getEmail()))
            throw new InvalidUserRegisterException("Invalid email format. Example: abc@gmail.com");

        if (request.getUserName() == null || !Pattern.matches(usernameRegex, request.getUserName()))
            throw new InvalidUserRegisterException("Invalid username. Use 4-20 alphanumeric characters or underscore.");

        if (request.getPassword() == null || !Pattern.matches(passwordRegex, request.getPassword()))
            throw new InvalidUserRegisterException("Weak password! Must have uppercase, lowercase, digit, special char, 8-20 chars.");

        if (request.getRole() == null || !Pattern.matches(roleRegex, request.getRole().toUpperCase()))
            throw new InvalidUserRegisterException("Invalid role. Must be USER or ADMIN.");

        return true;
    }

    @Override
    public AuthResponse login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found after authentication"));
            String token = jwtUtil.generateToken(username, user.getRole(), user.getUserId());
            return new AuthResponse(token, username, user.getRole(), "Login successful");
        }

        throw new RuntimeException("Authentication failed");
    }

    @Override
    public void logout(String username) {
        // For stateless JWT: logout is handled client-side by discarding the token.
        // Future: Add token to Redis blacklist here.
    }


    @Override
    public String refreshToken(String token) {
        return "";
    }

    @Override
    public UserProfileResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToProfile(user);
    }

    @Override
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToProfile(user);
    }

    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request) throws IOException {
        Long userId = extractUserId(request);
        String avatarUrl = cloudinaryService.uploadFile(file, "flowboard/avatars");
        userRepository.findById(userId).ifPresent(user -> {
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        });
        return avatarUrl;
    }

    @Override
    public UserProfileResponse getUserByUsername(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return mapToProfile(user);
    }

    @Override
    public UserProfileResponse updateProfile(Long id, RegisterRequest request) {
        return null;
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) throws InvalidUserOperationException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        if(user.getPasswordHash().equals(encoder.encode(oldPassword))){
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
            if (newPassword == null || !Pattern.matches(passwordRegex, newPassword)) {
                throw new InvalidUserOperationException("Weak password! Must have uppercase, lowercase, digit, special char, 8-20 chars.");
            }
            user.setPasswordHash(encoder.encode(newPassword));
            return "Successfully Changed the Password";
        }
        throw new RuntimeException("User's Old PassWord is Not Correct please try again.");
    }

    @Override
    public String deactivateAccount(Long id) throws InvalidUserOperationException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidUserOperationException("User not found with id: " + id));

        user.setActive(false);
        userRepository.save(user);
        return "User account deactivated successfully";
    }

    @Override
    public List<UserProfileResponse> searchUsersByFullName(String fullName) {
        return userRepository.searchByFullName(fullName)
                .stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileResponse> searchUsersByRole(String role) {
        return userRepository.findAllByRole(role)
                .stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
    }

    private UserProfileResponse mapToProfile(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setActive(user.isActive());
        return response;
    }

    private Long extractUserId(HttpServletRequest req){
        String header = req.getHeader("Authorization");
        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);
        Long userId = userRepository.findByUsername(username).orElseThrow().getUserId();
        return userId;
    }
}
