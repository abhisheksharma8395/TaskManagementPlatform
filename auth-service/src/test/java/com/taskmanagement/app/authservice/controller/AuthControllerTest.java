package com.taskmanagement.app.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.app.authservice.dto.*;
import com.taskmanagement.app.authservice.service.AuthService;
import com.taskmanagement.app.authservice.service.CloudinaryService;
import com.taskmanagement.app.authservice.util.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
        org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private CloudinaryService cloudinaryService;
    @MockitoBean
    private JWTUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private com.taskmanagement.app.authservice.service.TaskUserDetailsService taskUserDetailsService;

    private UserProfileResponse buildProfile() {
        UserProfileResponse p = new UserProfileResponse();
        p.setUserId(1L);
        p.setUsername("john_doe");
        p.setFullName("John Doe");
        p.setEmail("john@example.com");
        p.setRole("USER");
        p.setActive(true);
        return p;
    }

    @Test
    @WithMockUser
    void login_validCredentials_returns200() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("password");
        AuthResponse auth = new AuthResponse("tok", "john_doe", "USER", "Login successful");
        when(authService.login(anyString(), anyString())).thenReturn(auth);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"));
    }

    @Test
    @WithMockUser
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest("john_doe", "John Doe", "john@example.com", "StrongPass@1", "USER");
        when(authService.register(any(RegisterRequest.class))).thenReturn(buildProfile());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    @WithMockUser
    void getUserById_found_returns200() throws Exception {
        when(authService.getUserById(anyLong())).thenReturn(buildProfile());

        mockMvc.perform(get("/auth/user/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @WithMockUser
    void getUserByUsername_found_returns200() throws Exception {
        when(authService.getUserByUsername(anyString())).thenReturn(buildProfile());

        mockMvc.perform(get("/auth/user/username/john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    @WithMockUser
    void getUserByEmail_found_returns200() throws Exception {
        when(authService.getUserByEmail(anyString())).thenReturn(buildProfile());

        mockMvc.perform(get("/auth/user/email/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser
    void deactivateAccount_found_returns200() throws Exception {
        when(authService.deactivateAccount(anyLong())).thenReturn("User account deactivated successfully");

        mockMvc.perform(patch("/auth/deactivate/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User account deactivated successfully"));
    }

    @Test
    @WithMockUser
    void searchByName_returns200() throws Exception {
        when(authService.searchUsersByFullName(anyString())).thenReturn(List.of(buildProfile()));

        mockMvc.perform(get("/auth/search/name").param("fullName", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    @WithMockUser
    void searchByRole_returns200() throws Exception {
        when(authService.searchUsersByRole(anyString())).thenReturn(List.of(buildProfile()));

        mockMvc.perform(get("/auth/search/role/USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("USER"));
    }
}
