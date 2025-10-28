package com.question.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionOperationResponseDTO {
    private boolean success;
    private String message;
    private String questionId;
    private String operation; // "created", "updated"
}

