package com.quiz.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Marks this class as a Data Transfer Object (DTO) for quiz requests
@Data
public class QuizReqDTO {

    // Ensures the title is not blank and has a length between 3 and 100 characters
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    // Ensures the description is not blank and has a length between 3 and 255 characters
    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    private String description;

    private String createdByUserId;

    // Ensures the list of question IDs is not empty and each ID is not blank
    @NotEmpty(message = "At least one question ID is required")
    private List<@NotBlank(message = "Question ID cannot be blank") String> questionIds;

    // Optional fields for specifying the start and end time of the quiz
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}