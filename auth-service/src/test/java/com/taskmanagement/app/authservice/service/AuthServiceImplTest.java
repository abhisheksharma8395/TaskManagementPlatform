package com.taskmanagement.app.authservice.service;

import com.taskmanagement.app.authservice.dto.AuthResponse;
import com.taskmanagement.app.authservice.dto.RegisterRequest;
import com.taskmanagement.app.authservice.dto.UserProfileResponse;
import com.taskmanagement.app.authservice.entity.User;
import com.taskmanagement.app.authservice.exception.InvalidUserOperationException;
import com.taskmanagement.app.authservice.exception.InvalidUserRegisterException;
import com.taskmanagement.app.authservice.repository.UserRepository;
import com.taskmanagement.app.authservice.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setUsername("john_doe");
        sampleUser.setFullName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPasswordHash("$2a$hashed");
        sampleUser.setRole("USER");
        sampleUser.setActive(true);

        validRequest = new RegisterRequest(
                "john_doe", "John Doe", "john@example.com", "StrongPass@1", "USER");
    }

    // ─── register ────────────────────────────────────────────────────────────

    @Test
    void register_happyPath_returnsProfile() throws Exception {
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(validRequest.getUserName())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserProfileResponse result = authService.register(validRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("Email is already registered");
    }

    @Test
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(validRequest.getUserName())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("Username is already taken");
    }

    // ─── validatingUserRegister ───────────────────────────────────────────────

    @Test
    void validatingUserRegister_nullRequest_throwsException() {
        assertThatThrownBy(() -> authService.validatingUserRegister(null))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void validatingUserRegister_invalidFullName_throwsException() {
        RegisterRequest req = new RegisterRequest("user1234", "Ab", "a@b.com", "StrongPass@1", "USER");
        assertThatThrownBy(() -> authService.validatingUserRegister(req))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("full name");
    }

    @Test
    void validatingUserRegister_invalidEmail_throwsException() {
        RegisterRequest req = new RegisterRequest("user1234", "John Doe", "bad-email", "StrongPass@1", "USER");
        assertThatThrownBy(() -> authService.validatingUserRegister(req))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("email");
    }

    @Test
    void validatingUserRegister_invalidUsername_throwsException() {
        RegisterRequest req = new RegisterRequest("ab", "John Doe", "john@example.com", "StrongPass@1", "USER");
        assertThatThrownBy(() -> authService.validatingUserRegister(req))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("username");
    }

    @Test
    void validatingUserRegister_weakPassword_throwsException() {
        RegisterRequest req = new RegisterRequest("john_doe", "John Doe", "john@example.com", "weakpass", "USER");
        assertThatThrownBy(() -> authService.validatingUserRegister(req))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("password");
    }

    @Test
    void validatingUserRegister_invalidRole_throwsException() {
        RegisterRequest req = new RegisterRequest("john_doe", "John Doe", "john@example.com", "StrongPass@1",
                "SUPERUSER");
        assertThatThrownBy(() -> authService.validatingUserRegister(req))
                .isInstanceOf(InvalidUserRegisterException.class)
                .hasMessageContaining("role");
    }

    @Test
    void validatingUserRegister_validRequest_returnsTrue() throws Exception {
        boolean result = authService.validatingUserRegister(validRequest);
        assertThat(result).isTrue();
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_happyPath_returnsAuthResponse() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(jwtUtil.generateToken("john_doe", "USER", 1L)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login("john_doe", "password");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUsername()).isEqualTo("john_doe");
    }

    // ─── logout ───────────────────────────────────────────────────────────────

    @Test
    void logout_doesNotThrow() {
        authService.logout("john_doe"); // no-op, just verify no exception
    }

    // ─── getUserByEmail ───────────────────────────────────────────────────────

    @Test
    void getUserByEmail_found_returnsProfile() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        UserProfileResponse result = authService.getUserByEmail("john@example.com");

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserByEmail("x@x.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── getUserById ──────────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserProfileResponse result = authService.getUserById(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── getUserByUsername ────────────────────────────────────────────────────

    @Test
    void getUserByUsername_found_returnsProfile() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));

        UserProfileResponse result = authService.getUserByUsername("john_doe");

        assertThat(result.getUsername()).isEqualTo("john_doe");
    }

    @Test
    void getUserByUsername_notFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ─── deactivateAccount ────────────────────────────────────────────────────

    @Test
    void deactivateAccount_found_deactivates() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String result = authService.deactivateAccount(1L);

        assertThat(result).contains("deactivated");
        verify(userRepository).save(sampleUser);
    }

    @Test
    void deactivateAccount_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.deactivateAccount(999L))
                .isInstanceOf(InvalidUserOperationException.class)
                .hasMessageContaining("User not found");
    }

    // ─── searchUsersByFullName ────────────────────────────────────────────────

    @Test
    void searchUsersByFullName_returnsList() {
        when(userRepository.searchByFullName("John Doe")).thenReturn(List.of(sampleUser));

        List<UserProfileResponse> results = authService.searchUsersByFullName("John Doe");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFullName()).isEqualTo("John Doe");
    }

    // ─── searchUsersByRole ────────────────────────────────────────────────────

    @Test
    void searchUsersByRole_returnsList() {
        when(userRepository.findAllByRole("USER")).thenReturn(List.of(sampleUser));

        List<UserProfileResponse> results = authService.searchUsersByRole("USER");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRole()).isEqualTo("USER");
    }

    // ─── refreshToken ─────────────────────────────────────────────────────────

    @Test
    void refreshToken_returnsEmptyString() {
        String result = authService.refreshToken("some.token");
        assertThat(result).isEmpty();
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    void updateProfile_returnsNull() {
        UserProfileResponse result = authService.updateProfile(1L, validRequest);
        assertThat(result).isNull();
    }
}
