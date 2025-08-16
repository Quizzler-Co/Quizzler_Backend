package com.quiz.global_exception;

import com.quiz.custom_exception.ResourceAlreadyExistsException;
import com.quiz.custom_exception.ResourceNotFoundException;
import com.quiz.custom_exception.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Marks this class as a global exception handler for REST controllers
@RestControllerAdvice
public class GlobalException {

    // Handles exceptions of type ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        // Returns a 404 NOT FOUND response with the exception message
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // Handles exceptions of type ResourceAlreadyExistsException
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceAlreadyExistsException e) {
        // Returns a 404 NOT FOUND response with the exception message
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleResourceNotFoundException(UnauthorizedAccessException e) {
        // Returns a 401 NOT FOUND response with the exception message
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}