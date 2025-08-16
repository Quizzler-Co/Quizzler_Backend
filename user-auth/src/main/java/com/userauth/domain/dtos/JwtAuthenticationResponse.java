package com.userauth.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String name;
    private String username;
    private String email;
}
