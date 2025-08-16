package com.participation.entity;

import com.participation.entity.Answer;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "participations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participation {

    @Id
    private String participationId;

    private String userId;   // Reference to User (can be UUID or ObjectId as String)
    private String quizId;   // Reference to Quiz

    private Integer score;
    private LocalDateTime joinTime;
    private boolean submitted;

    @Builder.Default
    private List<Answer> answers = new ArrayList<>();  // Embedded documents
}
