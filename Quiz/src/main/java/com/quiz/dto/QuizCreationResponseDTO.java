package com.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizCreationResponseDTO {
    private String quizId;
    private String title;
    private String message;
    private List<String> questionIds;
    
    public QuizCreationResponseDTO(String quizId, String title, String message) {
        this.quizId = quizId;
        this.title = title;
        this.message = message;
    }
}

