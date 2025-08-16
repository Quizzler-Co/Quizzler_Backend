package com.userauth.controllers;

import com.userauth.domain.dtos.UserInfo;
import com.userauth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthValidationController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/validate")
    public ResponseEntity<UserInfo> validateToken(@RequestParam String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtTokenProvider.getUserIdFromJwt(token);
        String email = jwtTokenProvider.getEmailFromJwt(token);
        String role = jwtTokenProvider.getRoleFromJwt(token);

        UserInfo userInfo = new UserInfo(
                userId.toString(),
                email,
                role
        );

        return ResponseEntity.ok(userInfo);
    }
}
