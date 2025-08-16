package com.participation.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    private String questionId;
    private String selectedOption;
    private boolean isCorrect;
}
