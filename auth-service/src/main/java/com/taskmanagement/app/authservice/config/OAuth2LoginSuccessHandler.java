package com.taskmanagement.app.authservice.config;

import com.taskmanagement.app.authservice.entity.User;
import com.taskmanagement.app.authservice.repository.UserRepository;
import com.taskmanagement.app.authservice.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    JWTUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setFullName(name);
                    String username = email.split("@")[0] + "_" + (System.currentTimeMillis()%10000);
                    u.setUsername(username);
                    u.setPasswordHash("OAUTH_USER");
                    u.setProvider("GOOGLE");
                    u.setRole("USER");
                    u.setActive(true);
                    return userRepository.save(u);
                });
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        response.sendRedirect("http://localhost:5173/oauth-success?token=" + token);
    }
}
