package com.leaderboard.repository;

import com.leaderboard.Entity.Leaderboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends MongoRepository<Leaderboard,String> {
    boolean existsByUserIdAndQuizId(String userId, String quizId);

    Optional<Leaderboard> findByUserIdAndQuizId(String userId, String quizId);
    List<Leaderboard> findByQuizId(String quizId);
    List<Leaderboard> findByUserId(String userId);

}
