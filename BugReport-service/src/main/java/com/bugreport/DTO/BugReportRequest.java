package com.bugreport.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class BugReportRequest {
    private String quizId;        // Very important
    private String questionId;    // (optional, if granular)
    private String title;
    private String description;
    private String severity;      // LOW, MEDIUM, HIGH, CRITICAL
}
