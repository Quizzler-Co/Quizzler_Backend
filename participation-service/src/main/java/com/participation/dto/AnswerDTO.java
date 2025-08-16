package com.participation.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnswerDTO {
    private String questionId;
    private String selectedOption;
    private boolean isCorrect;
}

