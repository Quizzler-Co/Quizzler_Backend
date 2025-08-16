package com.quiz.sharedDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor

public class QuestionCreateRequest {
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String category;
    private String difficulty;
    private String explanation;
    private String quizId;
}
