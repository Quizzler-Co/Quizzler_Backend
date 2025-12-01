package com.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/quiz")
    public ResponseEntity<String> quizFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Quiz Service is unavailable. Please try again later.");
    }

    @GetMapping("/question")
    public ResponseEntity<String> questionFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Question Service is unavailable. Please try again later.");
    }

    @GetMapping("/participation")
    public ResponseEntity<String> participationFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Participation Service is unavailable. Please try again later.");
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<String> leaderboardFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Leaderboard Service is unavailable. Please try again later.");
    }

    @GetMapping("/bugreport")
    public ResponseEntity<String> bugReportFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("BugReport Service is unavailable. Please try again later.");
    }

    @GetMapping("/blog")
    public ResponseEntity<String> blogFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Blog Service is unavailable. Please try again later.");
    }

    @PostMapping("/blog")
    public ResponseEntity<String> blogPostFallback() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Blog Service is unavailable. Please try again later.");
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "API Gateway is running");
        response.put("port", 8086);
        response.put("message", "If you see this, the API Gateway is accessible");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
