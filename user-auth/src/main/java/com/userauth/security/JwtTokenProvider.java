package com.userauth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Long jwtExpirationTimeInMs;

    // We use constructor injection:
    public JwtTokenProvider(@Value("${app.jwtSecret}") String jwtSecret,
            @Value("${app.jwtExpirationInMs}") Long jwtExpirationTimeInMs) {

        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationTimeInMs = jwtExpirationTimeInMs;

    }

    // generating jwt token according to users credentials

    public String generateToken(Authentication authentication) {
        // getting user Object from authentication Class
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // gives the expiry time
        Instant now = Instant.now();
        Instant expiryInstant = now.plus(jwtExpirationTimeInMs, ChronoUnit.MILLIS);
        // extracting the role from userPrincipal
        String role = userPrincipal.getRole(userPrincipal.getAuthorities());
        return Jwts
                .builder()
                .subject(userPrincipal.getId().toString())
                .claim("email", userPrincipal.getEmail())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryInstant))
                .signWith(secretKey)
                .compact();

    }
    public String getEmailFromJwt(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }
    public String getRoleFromJwt(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }

    public Long getUserIdFromJwt(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey).build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.error("JWT validation error: {}", ex.getMessage());
        }
        return false;

    }
}
