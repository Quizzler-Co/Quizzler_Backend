package com.participation.service;

import com.participation.dto.QuestionDTO;
import com.participation.sharedDTO.QuizDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name="quiz-service")
public interface QuizClient {
    @GetMapping("/api/v1/quiz/questions/quiz/{quizId}")
    List<QuestionDTO> getQuestionsByQuizId(
            @PathVariable("quizId") String quizId,
            @RequestHeader("x-internal-call") String internalCall
    );

    @GetMapping("/api/v1/quiz/{quizId}")
    QuizDTO getQuizById(
            @PathVariable("quizId") String quizId,
            @RequestHeader("x-internal-call") String internalCall
    );
    @GetMapping("/api/v1/quiz/{quizId}/exists")
    boolean doesQuizExist(
            @PathVariable("quizId") String quizId,
            @RequestHeader("x-internal-call") String internalCall
    );
}
