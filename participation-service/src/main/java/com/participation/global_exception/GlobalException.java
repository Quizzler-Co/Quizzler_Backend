package com.participation.global_exception;

import com.participation.custom_exception.ResourceAlreadyExistsException;
import com.participation.custom_exception.ResourceNotFoundException;
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
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}