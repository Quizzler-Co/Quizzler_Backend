package com.quiz.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.quiz.custom_exception.ResourceAlreadyExistsException;
import com.quiz.custom_exception.ResourceNotFoundException;
import com.quiz.custom_exception.UnauthorizedAccessException;
import com.quiz.dto.QuizCreationRequest;
import com.quiz.dto.QuizCreationResponseDTO;
import com.quiz.dto.QuizOperationResponseDTO;
import com.quiz.dto.QuizReqDTO;
import com.quiz.dto.QuizWithQuestionsDTO;
import com.quiz.service.QuestionClient;
import com.quiz.sharedDTO.QuestionCreateRequest;
import com.quiz.sharedDTO.QuestionDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.quiz.dto.QuizDTO;
import com.quiz.entity.Quiz;
import com.quiz.repository.QuizRepository;
import com.quiz.service.QuizService;

import lombok.AllArgsConstructor;

// Marks this class as a service and enables dependency injection
@Service
@AllArgsConstructor // Generates a constructor with all fields as parameters
public class QuizServiceImpl implements QuizService {

    // Repository for interacting with the database
    private QuizRepository quizRepo;

    // Mapper for converting between entities and DTOs
    private ModelMapper mapper;

    private final QuestionClient questionClient;

    // Fetches all quizzes from the database
    @Override
    public List<QuizDTO> getAllQuiz() {
        // Retrieves all quizzes and throws an exception if none are found
        List<Quiz> quiz = quizRepo.findAll();
        if (quiz.size() < 0) {
            throw new ResourceNotFoundException("No Quiz found");
        }
        // Maps the list of Quiz entities to a list of QuizDTOs
        return quiz.stream().map(a -> mapper.map(a, QuizDTO.class)).collect(Collectors.toList());
    }

    // Fetches a specific quiz by its ID
    @Override
    public QuizDTO getQuiz(String id) {
        // Finds the quiz or throws an exception if not found
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        // Maps the Quiz entity to a QuizDTO
        return mapper.map(quiz, QuizDTO.class);
    }

    @Override
    public List<QuestionDTO> getQuestionsById(String quizId) {
        return questionClient.getQuestionsById(quizId,"true");
    }

        @Override
        public QuizCreationResponseDTO createQuizWithQuestions(QuizCreationRequest request, String userId) {
            if (quizRepo.existsByTitle(request.getTitle())) {
                throw new ResourceAlreadyExistsException("Title Already Exists");
            }

            // Create and save Quiz entity first (without questionIds)
            Quiz quiz = new Quiz();
            quiz.setTitle(request.getTitle());
            quiz.setDescription(request.getDescription());
            quiz.setCategory(request.getCategory());
            quiz.setTimePerQuestion(request.getTimePerQuestion());
            quiz.setCreatedByUserId(userId);
            quiz = quizRepo.save(quiz); // Save and get generated quiz ID
            String quizId= quiz.getId();
            // Map QuestionDTOs to QuestionCreateRequests, set quizId
            List<QuestionCreateRequest> questionCreateRequests = request.getQuestions().stream()
                .map(q -> {
                    QuestionCreateRequest qc = new QuestionCreateRequest();
                    qc.setQuestionText(q.getQuestionText());
                    qc.setOptions(q.getOptions());
                    qc.setCorrectAnswer(q.getCorrectAnswer());
                    qc.setCategory(q.getCategory());
                    qc.setDifficulty(q.getDifficulty());
                    qc.setExplanation(q.getExplanation());
                    qc.setQuizId(quizId); // Set quizId for bi-directional mapping
                    return qc;
                })
                .collect(Collectors.toList());

            // Call question-service to save questions and get IDs
            List<String> questionIds = questionClient.createQuestions(questionCreateRequests,"true");

            // Update quiz with questionIds and save again
            quiz.setQuestionIds(questionIds);
            quizRepo.save(quiz);

            QuizCreationResponseDTO response = new QuizCreationResponseDTO();
            response.setQuizId(quizId);
            response.setTitle(quiz.getTitle());
            response.setMessage("Quiz and questions created successfully");
            response.setQuestionIds(questionIds);
            return response;
        }


    // Deletes a quiz by its ID
    @Override
    public QuizOperationResponseDTO deleteQuiz(String id, String userId) {
        // Finds the quiz or throws an exception if not found
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        if (!quiz.getCreatedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to Delete this quiz.");
        }
        // Deletes the quiz from the database
        quizRepo.delete(quiz);
        
        QuizOperationResponseDTO response = new QuizOperationResponseDTO();
        response.setSuccess(true);
        response.setMessage("Quiz deleted successfully");
        response.setQuizId(id);
        response.setOperation("deleted");
        return response;
    }

    // Updates an existing quiz by its ID
    @Override
    public QuizDTO updateQuiz(String id, QuizDTO quizDTO,String userId) {
        // Finds the quiz or throws an exception if not found
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Quiz found"));
        // Checks if the user is authorized to update the quiz
        if (!quiz.getCreatedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this quiz.");
        }
        // Maps the updated data from QuizDTO to the existing Quiz entity
        mapper.map(quizDTO, quiz);
        // Saves the updated quiz to the database
        Quiz updated = quizRepo.save(quiz);
        // Maps the updated Quiz entity to a QuizDTO
        return mapper.map(updated, QuizDTO.class);
    }

    // Adds a new quiz to the database
    @Override
    public QuizOperationResponseDTO addQuiz(QuizReqDTO quizDTO) {
        // Checks if a quiz with the same title already exists
        if (quizRepo.existsByTitle(quizDTO.getTitle())) {
            throw new ResourceAlreadyExistsException("Title Already Exists");
        }
        // Maps the QuizReqDTO to a Quiz entity and saves it to the database
        Quiz savedQuiz = quizRepo.save(mapper.map(quizDTO, Quiz.class));
        
        QuizOperationResponseDTO response = new QuizOperationResponseDTO();
        response.setSuccess(true);
        response.setMessage("Quiz created successfully");
        response.setQuizId(savedQuiz.getId());
        response.setOperation("created");
        return response;
    }
    @Override
    public QuizWithQuestionsDTO getQuizWithQuestions(String quizId, boolean isPrivilegedUser) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));

        List<QuestionDTO> questions = questionClient.getQuestionsById(quizId,"true");

        // If not an admin/internal call, remove correct answers
        if (!isPrivilegedUser) {
            questions.forEach(q -> q.setCorrectAnswer(null));
        }

        // Create DTO to return
        QuizWithQuestionsDTO response = new QuizWithQuestionsDTO();
        response.setQuizId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setCategory(quiz.getCategory());
        // response.setDifficulty(quiz.getDifficulty());

        response.setTimePerQuestion(quiz.getTimePerQuestion());
        response.setQuestions(questions);
        return response;
    }
    @Override
    public List<QuizDTO> getQuizzesByCategory(String category) {
        List<Quiz> quizzes = quizRepo.findByCategoryIgnoreCase(category);
        return quizzes.stream()
                .map(q -> mapper.map(q, QuizDTO.class))
                .toList();
    }

    @Override
    public List<QuizDTO> getQuizzesByDifficulty(String difficulty) {
        List<Quiz> quizzes = quizRepo.findByDifficultyIgnoreCase(difficulty);
        return quizzes.stream()
                .map(q -> mapper.map(q, QuizDTO.class))
                .toList();
    }

    @Override
    public List<QuizDTO> getQuizzesByTitle(String title) {
        List<Quiz> quizzes = quizRepo.findByTitleIgnoreCase(title);
        return quizzes.stream()
                .map(q -> mapper.map(q, QuizDTO.class))
                .toList();
    }

}