package com.participation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SubmissionResultDTO {
    private String participantId;
    private List<AnswerDetail> answers; // List of answer details (submitted + correct + explanation)

    // Score details
    private int score;
    private int totalQuestions;
    private double percentage;

    // Getters and setters here

    @Setter
    @Getter
    public static class AnswerDetail {
        private String questionId;
        private String submittedAnswer;
        private String correctAnswer;
        private String explanation;

        // Getters and setters here
    }
}
