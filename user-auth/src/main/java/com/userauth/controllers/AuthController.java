package com.userauth.controllers;

import com.userauth.domain.dtos.*;
import com.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/login")//POST http://localhost:8086/api/v1/auth/login
    public ResponseEntity<JwtAuthenticationResponse> userLogin(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("========================================");
        System.out.println("[AUTH-CONTROLLER] Login request received");
        System.out.println("[AUTH-CONTROLLER] Username/Email: " + loginRequest.getUsernameOrEmail());
        System.out.println("[AUTH-CONTROLLER] Calling authService.login()...");
        
        try {
            JwtAuthenticationResponse jwtToken = authService.login(loginRequest);
            System.out.println("[AUTH-CONTROLLER] Login successful!");
            System.out.println("[AUTH-CONTROLLER] Token type: " + jwtToken.getTokenType());
            System.out.println("[AUTH-CONTROLLER] Token length: " + (jwtToken.getAccessToken() != null ? jwtToken.getAccessToken().length() : "NULL"));
            System.out.println("[AUTH-CONTROLLER] Username: " + jwtToken.getUsername());
            System.out.println("[AUTH-CONTROLLER] Email: " + jwtToken.getEmail());
            System.out.println("[AUTH-CONTROLLER] Name: " + jwtToken.getName());
            System.out.println("[AUTH-CONTROLLER] Returning response...");
            System.out.println("========================================");
            return ResponseEntity.ok(jwtToken);
        } catch (Exception e) {
            System.err.println("[AUTH-CONTROLLER] ERROR in login: " + e.getClass().getName());
            System.err.println("[AUTH-CONTROLLER] Error message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            throw e;
        }
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> userSignup(@Valid @RequestBody SignUpRequest signUpRequest) {
        System.out.println("========================================");
        System.out.println("[AUTH-CONTROLLER] Registration request received");
        System.out.println("[AUTH-CONTROLLER] Email: " + signUpRequest.getEmail());
        System.out.println("[AUTH-CONTROLLER] Username: " + signUpRequest.getUsername());
        System.out.println("[AUTH-CONTROLLER] Name: " + signUpRequest.getName());
        System.out.println("[AUTH-CONTROLLER] Role: " + signUpRequest.getRole());
        System.out.println("[AUTH-CONTROLLER] Calling authService.signUp()...");
        
        try {
            UserDto user = authService.signUp(signUpRequest);
            System.out.println("[AUTH-CONTROLLER] Registration successful! User ID: " + user.getId());
            System.out.println("[AUTH-CONTROLLER] Returning UserDto with userName: " + user.getUserName());
            System.out.println("========================================");
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            System.err.println("[AUTH-CONTROLLER] ERROR in registration: " + e.getClass().getName());
            System.err.println("[AUTH-CONTROLLER] Error message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            throw e;
        }
    }

    @PostMapping("/reset-password/{userId}")
    public ResponseEntity<ApiResponse> resetPassword(@PathVariable Long userId, @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        ApiResponse response = authService.resetPassword(resetPasswordRequest, userId);
        return ResponseEntity.ok(response);
    }
}
