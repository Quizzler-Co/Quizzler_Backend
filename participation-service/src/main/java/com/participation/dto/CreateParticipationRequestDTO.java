package com.participation.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateParticipationRequestDTO {
    private String userId;
    private String quizId;
}

