
package com.question.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionReqDTO {
    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Options are required")
    @Size(min = 2, message = "There must be at least 2 options")
    private List<@NotBlank(message = "Option cannot be blank") String> options;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @NotBlank(message = "Quiz ID is required")
    private String quizId;

}



