package com.participation.kafkaService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.participation.sharedDTO.QuizSubmitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final String TOPIC="quiz-submitted-v2";

    @Autowired
    private KafkaTemplate<String, QuizSubmitEvent> kafkaTemplate;

    public void sendQuizSubmit(QuizSubmitEvent quizSubmitEvent) throws JsonProcessingException {
        // This method sends a message to the Kafka topic "quiz-submitted"
        // The message is of type QuizSubmitEvent, which is a shared DTO
        System.out.println("SENDING EVENT: " + quizSubmitEvent);
        System.out.println("Serialized JSON: " + new ObjectMapper().writeValueAsString(quizSubmitEvent));
        kafkaTemplate.send(TOPIC, quizSubmitEvent);
    }

}
