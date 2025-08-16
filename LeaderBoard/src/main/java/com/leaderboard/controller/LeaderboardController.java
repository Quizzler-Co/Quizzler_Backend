package com.leaderboard.controller;

import com.leaderboard.DTO.LeaderboardRankDTO;
import com.leaderboard.DTO.LeaderboardRequestDTO;
import com.leaderboard.DTO.LeaderboardResponseDTO;
import com.leaderboard.service.LeaderboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

        // Submit or update score
        @PostMapping("/submit")
        public ResponseEntity<LeaderboardResponseDTO> submitScore(
                @Valid @RequestBody LeaderboardRequestDTO dto) {
            LeaderboardResponseDTO response = leaderboardService.submitScore(dto);
            return ResponseEntity.ok(response);
        }

        // Get a specific user's entry for a quiz
        @GetMapping("/entry/{userId}/{quizId}")
        public ResponseEntity<LeaderboardResponseDTO> getUserEntryForQuiz(
                @PathVariable String userId,
                @PathVariable String quizId) {
            LeaderboardResponseDTO response = leaderboardService.getEntryByUserAndQuiz(userId, quizId);
            return ResponseEntity.ok(response);
        }

        // Get leaderboard for a quiz (sorted, ranked)
        @GetMapping("/quiz/{quizId}")
        public ResponseEntity<?> getLeaderboardForQuiz(
                @PathVariable String quizId) {
            List<LeaderboardRankDTO> response = leaderboardService.getLeaderboardForQuiz(quizId);
            return ResponseEntity.ok(response);
        }

        // Get all entries by a specific user (across quizzes)
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<LeaderboardResponseDTO>> getAllEntriesByUser(
                @PathVariable String userId) {
            List<LeaderboardResponseDTO> response = leaderboardService.getAllEntriesByUser(userId);
            return ResponseEntity.ok(response);
        }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<?> deleteEntry(
            @PathVariable String entryId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("You are not authorized to delete leaderboard entries.");
        }

        leaderboardService.deleteLeaderboardEntry(entryId);
        return ResponseEntity.noContent().build();
    }

}
