package com.participation.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParticipationSummaryDTO {
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int score;
}

