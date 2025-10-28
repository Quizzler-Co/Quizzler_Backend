package com.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessDeniedResponseDTO {
    private boolean success;
    private String message;
    private String requiredRole;
    
    public AccessDeniedResponseDTO(String message, String requiredRole) {
        this.success = false;
        this.message = message;
        this.requiredRole = requiredRole;
    }
}

