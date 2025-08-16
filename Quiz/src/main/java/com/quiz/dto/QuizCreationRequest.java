package com.quiz.dto;

import com.quiz.sharedDTO.QuestionDTO;
import lombok.Data;

import java.util.List;

@Data
public class QuizCreationRequest {

    private String title;

    private String description;

    private String category;

    private int timePerQuestion;

    private String createdByUserId; // set internally or passed explicitly

    private List<QuestionDTO> questions; // inline questions to save
}
