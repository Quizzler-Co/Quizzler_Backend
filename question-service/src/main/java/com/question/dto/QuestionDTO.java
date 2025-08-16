package com.question.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Data
@AllArgsConstructor
@NoArgsConstructor

public class QuestionDTO implements Serializable {
    private String id;
    private String quizId; // optional reference to parent quiz 
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String category;
    private String difficulty;
    private String explanation;
}
