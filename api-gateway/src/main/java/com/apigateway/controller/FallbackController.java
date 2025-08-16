package com.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/quiz")
    public ResponseEntity<String> quizFallback() {
        return ResponseEntity.ok("Quiz Service is unavailable. Please try again later.");
    }

    @GetMapping("/question")
    public ResponseEntity<String> questionFallback() {
        return ResponseEntity.ok("Question Service is unavailable. Please try again later.");
    }

    @GetMapping("/participation")
    public ResponseEntity<String> participationFallback() {
        return ResponseEntity.ok("Participation Service is unavailable. Please try again later.");
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<String> leaderboardFallback() {
        return ResponseEntity.ok("Leaderboard Service is unavailable. Please try again later.");
    }

    // changes for cors
    @GetMapping("/user-auth")
    public ResponseEntity<String> userAuthGetFallback() {
        return ResponseEntity.ok("UserAuth Service is unavailable. Please try again later.");
    }

    @PostMapping("/user-auth")
    public ResponseEntity<String> userAuthPostFallback() {
        return ResponseEntity.ok("UserAuth Service is unavailable. Please try again later.");
    }

    @GetMapping("/bugreport")
    public ResponseEntity<String> bugReportFallback() {
        return ResponseEntity.ok("BugReport Service is unavailable. Please try again later.");
    }
}
