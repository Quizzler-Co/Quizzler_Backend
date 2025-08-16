package com.leaderboard.kafkaService;

import com.leaderboard.DTO.LeaderboardRequestDTO;
import com.leaderboard.service.LeaderboardService;
import com.leaderboard.sharedDTO.QuizSubmitEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final LeaderboardService leaderboardService;
    private final ModelMapper modelMapper;

    @KafkaListener(topics = "quiz-submitted-v2", groupId = "leaderboard-group")
    public void consume(QuizSubmitEvent quizSubmitEvent) {
        try {
            System.out.println("Received event: " + quizSubmitEvent);

            if (quizSubmitEvent == null || quizSubmitEvent.getUserId() == null) {
                System.out.println("Skipping null or malformed event");
                return;
            }

            System.out.println("Received quiz for user: " + quizSubmitEvent.getUserId());
            LeaderboardRequestDTO dto = modelMapper.map(quizSubmitEvent, LeaderboardRequestDTO.class);
            leaderboardService.submitScore(dto);
        } catch (Exception e) {
            // Log the real error
            e.printStackTrace();
            // Optionally, send to a dead-letter topic or alert
        }
    }
}
