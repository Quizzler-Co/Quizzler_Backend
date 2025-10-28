package com.question.service;

import com.question.dto.QuestionCreateRequest;
import com.question.dto.QuestionDTO;
import com.question.dto.QuestionOperationResponseDTO;
import com.question.dto.QuestionReqDTO;

import java.util.List;

public interface QuestionService {
    List<QuestionDTO> getAllQuestion();
    QuestionOperationResponseDTO createNewQuestion(QuestionReqDTO questionDTO);

    List<QuestionDTO> getAllQuestionByQuizId(String quizId);

    QuestionDTO getQuestionById(String id);
    QuestionOperationResponseDTO updateQuestion(String id, QuestionReqDTO questionDTO);

    List<String> createQuestions(List<QuestionCreateRequest> requests);
}
