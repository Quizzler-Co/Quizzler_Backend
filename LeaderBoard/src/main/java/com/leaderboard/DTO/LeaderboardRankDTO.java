package com.leaderboard.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardRankDTO {

    private String userName;  // Display name
    private int score;
    private int rank;
}

