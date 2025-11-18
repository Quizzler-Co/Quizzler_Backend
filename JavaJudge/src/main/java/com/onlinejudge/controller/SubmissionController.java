package com.onlinejudge.controller;

import com.onlinejudge.dto.SubmissionRequest;
import com.onlinejudge.dto.SubmissionResponse;
import com.onlinejudge.service.JudgeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private static final Logger logger = Logger.getLogger(SubmissionController.class.getName());

    @Autowired
    private JudgeService judgeService;

    @PostMapping("/submit")
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmissionRequest request) {
        logger.info("=== SUBMISSION REQUEST RECEIVED ===");
        logger.info("Problem ID: " + request.getProblemId());
        logger.info("Code length: " + (request.getCode() != null ? request.getCode().length() : 0));
        logger.info("Thread: " + Thread.currentThread().getName());
        
        try {
            logger.info("Calling judgeService.judge()...");
            SubmissionResponse response = judgeService.judge(request);
            logger.info("judgeService.judge() completed successfully");
            
            // Ensure response is never null
            if (response == null) {
                logger.severe("JudgeService returned null response for problemId: " + request.getProblemId());
                response = new SubmissionResponse("ERROR", "Judge service returned null response. Please check server logs.");
            }
            
            // Log the response details
            logger.info("Returning response - Verdict: " + response.getVerdict() + ", Message: " + 
                (response.getMessage() != null ? response.getMessage().substring(0, Math.min(100, response.getMessage().length())) : "null"));
            
            return ResponseEntity.ok(response);
        } catch (jakarta.validation.ConstraintViolationException e) {
            logger.severe("Validation error: " + e.getMessage());
            SubmissionResponse errorResponse = new SubmissionResponse("ERROR", 
                "Validation error: " + (e.getMessage() != null ? e.getMessage() : "Invalid request"));
            return ResponseEntity.ok(errorResponse);
        } catch (Exception e) {
            logger.severe("Exception in submit endpoint: " + e.getMessage());
            logger.severe("Exception type: " + e.getClass().getName());
            logger.severe("Stack trace: " + getStackTrace(e));
            SubmissionResponse errorResponse = new SubmissionResponse("ERROR", 
                "Unexpected error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}

