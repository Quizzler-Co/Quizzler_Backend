package com.question.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateRequest {

    private String quizId;

    private String questionText;

    private List<String> options;

    private String correctAnswer;

    private String category;

    private String difficulty;

    private String explanation;
}

