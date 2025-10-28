package com.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    
    public OperationResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

