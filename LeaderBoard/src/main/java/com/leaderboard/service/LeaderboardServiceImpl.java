package com.leaderboard.service;

import com.leaderboard.DTO.LeaderboardRankDTO;
import com.leaderboard.DTO.LeaderboardRequestDTO;
import com.leaderboard.DTO.LeaderboardResponseDTO;
import com.leaderboard.Entity.Leaderboard;
import com.leaderboard.custom_exception.ResourceNotFoundException;
import com.leaderboard.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;

    @Override
    public LeaderboardResponseDTO submitScore(LeaderboardRequestDTO dto) {
        Optional<Leaderboard> existing = leaderboardRepository.findByUserIdAndQuizId(dto.getUserId(), dto.getQuizId());

        Leaderboard leaderboard = existing.orElse(new Leaderboard());
        leaderboard.setUserId(dto.getUserId());
        leaderboard.setQuizId(dto.getQuizId());
        leaderboard.setScore(dto.getScore());
        System.out.println("Sending internal call header as: true");
        // Fetch username from user service
        Map<String, String> userMap = userClient.getUserNames(
                List.of(Long.parseLong(dto.getUserId()))
        );

        String userName = userMap.getOrDefault(dto.getUserId(), "Unknown");

        leaderboard.setUserName(userName); // Set the username in the leaderboard entry
        leaderboard = leaderboardRepository.save(leaderboard); // Save after setting userName

        LeaderboardResponseDTO responseDTO = modelMapper.map(leaderboard, LeaderboardResponseDTO.class);
        responseDTO.setUserName(userName);
        responseDTO.setRank(0);

        return responseDTO;
    }


    @Override
    public LeaderboardResponseDTO getEntryByUserAndQuiz(String userId, String quizId) {
        Leaderboard leaderboard = leaderboardRepository
                .findByUserIdAndQuizId(userId, quizId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leaderboard entry not found for userId: " + userId + " and quizId: " + quizId));

        String userName = getUserNameById(userId);

        LeaderboardResponseDTO dto = modelMapper.map(leaderboard, LeaderboardResponseDTO.class);
        dto.setUserName(userName);
        dto.setRank(0);
        return dto;
    }

    @Override
    public List<LeaderboardRankDTO> getLeaderboardForQuiz(String quizId) {
        List<Leaderboard> entries = leaderboardRepository.findByQuizId(quizId);

        if (entries.isEmpty()) {
            throw new ResourceNotFoundException("No leaderboard entries found for quizId: " + quizId);
        }

        entries.sort(Comparator.comparingInt(Leaderboard::getScore).reversed());

        List<Long> userIds = entries.stream()
                .map(entry -> Long.parseLong(entry.getUserId()))
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> userMap = userClient.getUserNames(userIds);

        int[] rank = {1};
        return entries.stream()
                .map(entry -> LeaderboardRankDTO.builder()
                        .userName(userMap.getOrDefault(entry.getUserId(), "Unknown"))
                        .score(entry.getScore())
                        .rank(rank[0]++)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaderboardResponseDTO> getAllEntriesByUser(String userId) {
        List<Leaderboard> entries = leaderboardRepository.findByUserId(userId);

        if (entries.isEmpty()) {
            throw new ResourceNotFoundException("No leaderboard entries found for userId: " + userId);
        }

        String userName = getUserNameById(userId);

        entries.sort(Comparator.comparingInt(Leaderboard::getScore).reversed());

        return entries.stream()
                .map(entry -> {
                    LeaderboardResponseDTO dto = modelMapper.map(entry, LeaderboardResponseDTO.class);
                    dto.setUserName(userName);
                    dto.setRank(0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLeaderboardEntry(String leaderboardEntryId) {
        if (!leaderboardRepository.existsById(leaderboardEntryId)) {
            throw new ResourceNotFoundException("Leaderboard entry not found with ID: " + leaderboardEntryId);
        }
        leaderboardRepository.deleteById(leaderboardEntryId);
    }

    private String getUserNameById(String userId) {
        Long id = Long.parseLong(userId);
        Map<String, String> userMap = userClient.getUserNames(List.of(id));
        return userMap.getOrDefault(userId, "Unknown");
    }
}
