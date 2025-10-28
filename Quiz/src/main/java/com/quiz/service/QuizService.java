package com.quiz.service;

import java.util.List;

import com.quiz.dto.QuizCreationRequest;
import com.quiz.dto.QuizCreationResponseDTO;
import com.quiz.dto.QuizDTO;
import com.quiz.dto.QuizOperationResponseDTO;
import com.quiz.dto.QuizReqDTO;
import com.quiz.dto.QuizWithQuestionsDTO;
import com.quiz.sharedDTO.QuestionDTO;

// Interface defining the contract for QuizService
public interface QuizService {

    // Method to fetch all quizzes
    List<QuizDTO> getAllQuiz();

    // Method to add a new quiz
    QuizOperationResponseDTO addQuiz(QuizReqDTO quizDTO);

    // Method to update an existing quiz by its ID
    QuizDTO updateQuiz(String id, QuizDTO quizDTO,String userId);

    // Method to delete a quiz by its ID
    QuizOperationResponseDTO deleteQuiz(String id,String userId);

    // Method to fetch a specific quiz by its ID
    QuizDTO getQuiz(String id);

    List<QuestionDTO> getQuestionsById(String quizId);

    QuizCreationResponseDTO createQuizWithQuestions(QuizCreationRequest request,String userId);

    QuizWithQuestionsDTO getQuizWithQuestions(String quizId, boolean isPrivilegedUser);

    List<QuizDTO> getQuizzesByCategory(String category);
    List<QuizDTO> getQuizzesByDifficulty(String difficulty);
    List<QuizDTO> getQuizzesByTitle(String title);

}
