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
        System.out.println("[Instance-B] Received login request from user: xyz");
        JwtAuthenticationResponse jwtToken = authService.login(loginRequest);
        return ResponseEntity.ok(jwtToken);
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> userSignup(@Valid @RequestBody SignUpRequest signUpRequest) {
        UserDto user = authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/reset-password/{userId}")
    public ResponseEntity<ApiResponse> resetPassword(@PathVariable Long userId, @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        ApiResponse response = authService.resetPassword(resetPasswordRequest, userId);
        return ResponseEntity.ok(response);
    }
}
