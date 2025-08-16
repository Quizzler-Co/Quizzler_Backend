package com.leaderboard.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "leaderboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard {

    // Unique identifier for each leaderboard entry (Primary Key)
    @Id
    private String leaderBoardEntryId;

    // Unique identifier of the user (Foreign Key to User)
    private String userId;

    private String userName;
    // Unique identifier of the quiz (Foreign Key to Quiz)
    private String quizId;

    // Final score achieved by the user in the quiz
    private int score;

    // User's rank in the quiz leaderboard (optional at insert, computed later)
    private int rank;                 // Computed later (optional at insert)
}
