package com.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizOperationResponseDTO {
    private boolean success;
    private String message;
    private String quizId;
    private String operation; // "created", "updated", "deleted"
}

