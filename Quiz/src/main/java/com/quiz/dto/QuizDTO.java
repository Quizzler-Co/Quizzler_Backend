package com.quiz.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class QuizDTO {
	private String id;
	private String title;
	private String description; // Added field
	private String createdByUserId;
	private List<String> questionIds;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}
