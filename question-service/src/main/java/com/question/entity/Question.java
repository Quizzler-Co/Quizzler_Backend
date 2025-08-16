package com.question.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    private String quizId; // optional reference to parent quiz

    private String questionText;

    private List<String> options;

    private String correctAnswer;

    private String category;

    private String difficulty;

    private String explanation;
}
