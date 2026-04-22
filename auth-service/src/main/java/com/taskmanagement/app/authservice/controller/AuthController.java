package com.taskmanagement.app.authservice.controller;

import com.taskmanagement.app.authservice.dto.UserLoginResponse;
import com.taskmanagement.app.authservice.dto.UserRegisterResponse;
import com.taskmanagement.app.authservice.entity.User;
import com.taskmanagement.app.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginResponse userResponse){
        try {
            return new ResponseEntity<>(authService.login(userResponse.getUsername(), userResponse.getPassword()),HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterResponse userResponse){
        try {
            authService.register(userResponse);
            return new ResponseEntity<>("Successfully Registered",HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
}
