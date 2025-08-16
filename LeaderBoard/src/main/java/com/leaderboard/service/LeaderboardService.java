package com.leaderboard.service;

import com.leaderboard.DTO.LeaderboardRankDTO;
import com.leaderboard.DTO.LeaderboardRequestDTO;
import com.leaderboard.DTO.LeaderboardResponseDTO;

import java.util.List;

public interface LeaderboardService {
    LeaderboardResponseDTO submitScore(LeaderboardRequestDTO dto);

    LeaderboardResponseDTO getEntryByUserAndQuiz(String userId, String quizId);

    List<LeaderboardRankDTO> getLeaderboardForQuiz(String quizId);

    List<LeaderboardResponseDTO> getAllEntriesByUser(String userId);

    void deleteLeaderboardEntry(String leaderboardEntryId);
}
