package com.quiz.controller;

import com.quiz.dto.QuizCreationRequest;
import com.quiz.dto.QuizDTO;
import com.quiz.dto.QuizReqDTO;
import com.quiz.repository.QuizRepository;
import com.quiz.sharedDTO.QuestionDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.quiz.service.QuizService;

import lombok.AllArgsConstructor;

import java.util.List;

// Marks this class as a REST controller and maps it to handle HTTP requests
@RestController
@RequestMapping("/api/v1/quiz") // Base URL for all endpoints in this controller
@AllArgsConstructor // Generates a constructor with all fields as parameters
public class QuizController {

    // Service layer dependency for handling business logic
    private QuizService quizService;
    private QuizRepository quizRepo;

    // Endpoint to fetch all quizzes
    @GetMapping("/")
    public ResponseEntity<?> getAllQuiz() {
        // Returns a list of all quizzes with HTTP 200 status
        return ResponseEntity.ok(quizService.getAllQuiz());
    }

    // Create new quiz (ADMIN only)
    @PostMapping("/")
    public ResponseEntity<?> createNewQuiz(
            @Valid @RequestBody QuizReqDTO DTO,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Admins only");
        }

        DTO.setCreatedByUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.addQuiz(DTO));
    }

    // Create quiz with questions (ADMIN only)
    @PostMapping("/create")
    public ResponseEntity<?> createQuizWithQuestions(
            @Valid @RequestBody QuizCreationRequest request,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Admins only");
        }

        request.setCreatedByUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuizWithQuestions(request, userId));
    }
    // Update quiz (ADMIN only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(
            @PathVariable String id,
            @RequestBody QuizDTO quizDTO,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Admins only");
        }

        quizDTO.setCreatedByUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.updateQuiz(id, quizDTO, userId));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(
            @PathVariable String id,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {
        System.out.println(" Inside deleteQuiz - userId: " + userId + ", role: " + role);
        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Admins only");
        }

        return ResponseEntity.status(HttpStatus.OK).body(quizService.deleteQuiz(id, userId));
    }

    // Endpoint to fetch a specific quiz by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable String id) {
        // Returns the requested quiz with HTTP 200 status
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    // Endpoint to fetch questions by quiz ID
    @GetMapping("/questions/quiz/{quizId}")
    public ResponseEntity<?> getQuestionsById(
            @PathVariable String quizId,
            @RequestHeader(value = "x-internal-call", required = false) String internalCall,
            @RequestHeader(value = "x-role", required = false) String role) {

        List<QuestionDTO> questions = quizService.getQuestionsById(quizId);

        boolean isInternal = "true".equalsIgnoreCase(internalCall);
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role);

        if (!(isInternal || isAdmin)) {
            questions.forEach(q -> q.setCorrectAnswer(null));
        }

        return ResponseEntity.ok(questions);
    }
    @GetMapping("/with-questions/{quizId}")
    public ResponseEntity<?> getQuizWithQuestions(
            @PathVariable String quizId,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role,
            @RequestHeader(value = "x-internal-call", required = false) String internalCall
    ) {
        boolean isInternal = "true".equalsIgnoreCase(internalCall);
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role);

        return ResponseEntity.ok(quizService.getQuizWithQuestions(quizId, isAdmin || isInternal));
    }
    // Get quizzes by category
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getQuizzesByCategory(
            @PathVariable String category,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        return ResponseEntity.ok(quizService.getQuizzesByCategory(category));
    }

    // Get quizzes by difficulty
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<?> getQuizzesByDifficulty(
            @PathVariable String difficulty,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        return ResponseEntity.ok(quizService.getQuizzesByDifficulty(difficulty));
    }

    // Get quizzes by title (case-insensitive, exact match)
    @GetMapping("/title/{title}")
    public ResponseEntity<?> getQuizzesByTitle(
            @PathVariable String title,
            @RequestHeader("x-user-id") String userId,
            @RequestHeader("x-role") String role) {

        return ResponseEntity.ok(quizService.getQuizzesByTitle(title));
    }
    @GetMapping("/{quizId}/exists")
    public ResponseEntity<Boolean> doesQuizExist(@PathVariable String quizId) {
        boolean exists = quizRepo.existsById(quizId);
        return ResponseEntity.ok(exists);
    }

}