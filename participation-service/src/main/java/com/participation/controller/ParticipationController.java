package com.participation.controller;

import com.participation.dto.*;
import com.participation.kafkaService.KafkaProducerService;
import com.participation.service.ParticipationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/participation")
@AllArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;
    private final KafkaProducerService producer;

    @PostMapping("/quiz/{quizId}")
    public ResponseEntity<?> saveParticipation(
            @PathVariable String quizId,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only users or admins can participate.");
        }

        CreateParticipationRequestDTO participation = new CreateParticipationRequestDTO();
        participation.setUserId(userId);
        participation.setQuizId(quizId);

        return ResponseEntity.ok(participationService.createParticipant(participation));
    }

    @GetMapping("/{participantId}")
    public ResponseEntity<?> getAllParticipationsById(
            @PathVariable String participantId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        return ResponseEntity.ok(participationService.getParticipationById(participantId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getParticipationsByUserId(
            @PathVariable String userId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        List<ParticipationResponseDTO> participations = participationService.getParticipationsByUserId(userId);
        return ResponseEntity.ok(participations);
    }

    @GetMapping("/user/{userId}/quiz/{quizId}")
    public ResponseEntity<?> getParticipationsByUserIdAndQuizId(
            @PathVariable String userId,
            @PathVariable String quizId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        List<ParticipationResponseDTO> participations = participationService.getParticipationsByQuizIdAndUserId(userId, quizId);
        return ResponseEntity.ok(participations);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<?> getParticipationsByQuizId(
            @PathVariable String quizId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        List<ParticipationResponseDTO> participations = participationService.getParticipationsByQuizId(quizId);
        return ResponseEntity.ok(participations);
    }

    @PostMapping("/{participantId}/submit")
    public ResponseEntity<?> submitQuizAnswers(
            @PathVariable String participantId,
            @RequestBody AnswerSubmissionDTO submissionDTO,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only users or admins can submit answers.");
        }

        SubmissionResultDTO result = participationService.submitAnswers(participantId, submissionDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{participationId}/answers")
    public ResponseEntity<?> getAnswersByParticipationId(
            @PathVariable String participationId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_USER".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        return ResponseEntity.ok(participationService.getParticpantsAnswers(participationId));
    }

    @DeleteMapping("/{participationId}")
    public ResponseEntity<?> deleteParticipation(
            @PathVariable String participationId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only admins can delete participation records.");
        }

        participationService.deleteParticipation(participationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllParticipations(
            @RequestHeader("x-role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Only admins can view all participation records.");
        }

        List<ParticipationResponseDTO> participations = participationService.getAllParticipations();
        return ResponseEntity.ok(participations);
    }
}
