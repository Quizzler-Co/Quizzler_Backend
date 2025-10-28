package com.question.controller;

import com.question.dto.AccessDeniedResponseDTO;
import com.question.dto.QuestionCreateRequest;
import com.question.dto.QuestionReqDTO;
import com.question.dto.QuestionDTO;
import com.question.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/question")
@AllArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // Admin-only: fetch all questions
    @GetMapping("/all")
    public ResponseEntity<?> getAllQuestion(@RequestHeader(value = "x-role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            AccessDeniedResponseDTO response = new AccessDeniedResponseDTO("Access Denied: Admin role required", "ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(questionService.getAllQuestion());
    }

    // Admin-only: create a question
    @PostMapping("/")
    public ResponseEntity<?> addQuestion(@RequestBody QuestionReqDTO questionDTO,
                                         @RequestHeader(value = "x-role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            AccessDeniedResponseDTO response = new AccessDeniedResponseDTO("Access Denied: Admin role required", "ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createNewQuestion(questionDTO));
    }

    // Internal or Admin: fetch questions by quiz ID
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<?> getQuestionByQuizId(@PathVariable String quizId,
                                                 @RequestHeader(value = "x-internal-call", required = false) String internalCall,
                                                 @RequestHeader(value = "x-role", required = false) String role) {
        List<QuestionDTO> questions = questionService.getAllQuestionByQuizId(quizId);

        boolean isInternal = "true".equalsIgnoreCase(internalCall);
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role);

        if (!(isInternal || isAdmin)) {
            questions.forEach(q -> q.setCorrectAnswer(null));
        }

        return ResponseEntity.ok(questions);
    }

    // Admin-only: fetch individual question
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable String id,
                                             @RequestHeader(value = "x-role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            AccessDeniedResponseDTO response = new AccessDeniedResponseDTO("Access Denied: Admin role required", "ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    // Admin-only: update question
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable String id,
                                            @RequestBody QuestionReqDTO questionDTO,
                                            @RequestHeader(value = "x-role", required = false) String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            AccessDeniedResponseDTO response = new AccessDeniedResponseDTO("Access Denied: Admin role required", "ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(questionService.updateQuestion(id, questionDTO));
    }

    // Internal or Admin: bulk create questions
    @PostMapping("/bulk")
    public ResponseEntity<?> createQuestions(@RequestBody List<QuestionCreateRequest> requests,
                                             @RequestHeader(value = "x-internal-call", required = false) String internalCall,
                                             @RequestHeader(value = "x-role", required = false) String role) {
        boolean isInternal = "true".equalsIgnoreCase(internalCall);
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role);

        if (!(isInternal || isAdmin)) {
            AccessDeniedResponseDTO response = new AccessDeniedResponseDTO("Access Denied", "ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        List<String> questionIds = questionService.createQuestions(requests);
        return ResponseEntity.ok(questionIds);
    }
}
