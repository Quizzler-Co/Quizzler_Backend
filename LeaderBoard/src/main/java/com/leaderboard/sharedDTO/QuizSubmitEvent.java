package com.leaderboard.sharedDTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class QuizSubmitEvent {
    private String userId;
    private String quizId;
    private int score;
}




