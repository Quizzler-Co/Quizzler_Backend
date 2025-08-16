package com.bugreport.entities;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bug_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BugReport {
    @Id
    private String id;

    private String quizId;        //  Links bug to quiz
    private String questionId;    // (optional)
    private String title;
    private String description;
    private String severity;
    private String reporterEmail;
    private String status = "OPEN";
    private LocalDateTime createdAt = LocalDateTime.now();
}
