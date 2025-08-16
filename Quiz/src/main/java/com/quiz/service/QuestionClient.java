package com.quiz.service;

import com.quiz.sharedDTO.QuestionCreateRequest;
import com.quiz.sharedDTO.QuestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "question-service")
public interface QuestionClient {

    @GetMapping("/api/v1/question/quiz/{quizId}")
    List<QuestionDTO> getQuestionsById(
            @PathVariable String quizId,
            @RequestHeader("x-internal-call") String internalCall
    );

    @PostMapping("/api/v1/question/bulk")
    List<String> createQuestions(
            @RequestBody List<QuestionCreateRequest> requests,
            @RequestHeader("x-internal-call") String internalCall
    );
}

