package com.quiz.sharedDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private String id;
    private String quizId; // optional reference to parent quiz
    private String questionText;

    private List<String> options;

    private String correctAnswer;

    private String category;

    private String difficulty;

    private String explanation;
}
