package com.participation.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParticipationResponseDTO {
    private String participationId;
    private String userId;
    private String quizTitle;
    private boolean submitted;
    private Integer score;
    private LocalDateTime joinTime;
}

