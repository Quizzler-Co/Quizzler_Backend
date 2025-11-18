package com.onlinejudge.repository;

import com.onlinejudge.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, String> {
    List<Problem> findByActiveTrue();
    List<Problem> findByActiveTrueAndDifficulty(String difficulty);
}

