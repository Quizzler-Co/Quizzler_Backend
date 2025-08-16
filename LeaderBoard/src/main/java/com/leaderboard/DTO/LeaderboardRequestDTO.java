package com.leaderboard.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardRequestDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String username;
    @NotBlank(message = "Quiz ID is required")
    private String quizId;

    @Min(value = 0, message = "Score cannot be negative")
    private int score;
}

