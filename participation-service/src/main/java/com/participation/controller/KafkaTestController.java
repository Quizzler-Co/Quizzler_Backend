package com.participation.controller;

import com.participation.kafkaService.KafkaProducerService;
import com.participation.sharedDTO.QuizSubmitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaProducerService kafkaProducerService;

    @GetMapping("/send")
    public String sendTestEvent() {
        QuizSubmitEvent event = new QuizSubmitEvent("usertest99", "quiz777", 4);
        try {
            kafkaProducerService.sendQuizSubmit(event);
            return "Event sent to Kafka.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send event: " + e.getMessage();
        }
    }
}
