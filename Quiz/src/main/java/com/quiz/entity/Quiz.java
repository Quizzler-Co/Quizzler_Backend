package com.quiz.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "quizzes")
public class Quiz {

	@Id
	private String id;

	private String title;

	private String description;

	private String difficulty; // e.g., Easy, Medium, Hard

	private String category;

	private int timePerQuestion; // time per question in seconds
	private String createdByUserId;
	private List<String> questionIds; // reference to actual Question IDs
}
