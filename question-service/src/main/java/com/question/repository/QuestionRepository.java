package com.question.repository;

import com.question.entity.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    Boolean existsByQuestionTextAndQuizId(String text,String quizId);
    List<Question> findAllByQuizId(String quizId);
}
