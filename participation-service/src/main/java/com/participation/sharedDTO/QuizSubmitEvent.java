package com.participation.sharedDTO;


import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class QuizSubmitEvent {
        private String userId;
        private String quizId;
        private int score;
    }



