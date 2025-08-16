package com.quiz.dto;

import com.quiz.sharedDTO.QuestionDTO;
import lombok.Data;

import java.util.List;

@Data
public class QuizWithQuestionsDTO {
    private String quizId;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private List<QuestionDTO> questions;
    private int timePerQuestion;

}

