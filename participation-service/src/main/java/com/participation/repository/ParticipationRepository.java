package com.participation.repository;

import com.participation.entity.Participation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends MongoRepository<Participation, String> {

    // Custom query methods can be defined here if needed
    // For example, to find participations by userId or quizId

    // Example:
    // List<Participation> findByUserId(String userId);
    // List<Participation> findByQuizId(String quizId);
    // These methods can be automatically implemented by Spring Data MongoDB

    Boolean existsByUserIdAndQuizId(String userId, String quizId);
    List<Participation> findAllByUserId(String userId);
    List<Participation> findAllByQuizId(String quizId);

    List<Participation> findAllByUserIdAndQuizId(String userId, String quizId);
}
