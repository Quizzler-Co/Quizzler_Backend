package com.leaderboard.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponseDTO {

    private String leaderboardEntryId;  // Unique entry ID
    private String userId;
    private String quizId;
    private int score;
    private int rank;

    // Optional: For UI display
    private String userName;
}

