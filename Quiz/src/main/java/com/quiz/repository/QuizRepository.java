package com.quiz.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quiz.entity.Quiz;

import java.util.List;
import java.util.Optional;

// Marks this interface as a Spring Data repository
@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    // Custom query method to check if a quiz with the given title exists
    Boolean existsByTitle(String name);
    List<Quiz> findByCategoryIgnoreCase(String category);
    List<Quiz> findByDifficultyIgnoreCase(String difficulty);
    List<Quiz> findByTitleIgnoreCase(String title);
}