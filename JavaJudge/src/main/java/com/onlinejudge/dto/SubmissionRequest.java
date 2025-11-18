package com.onlinejudge.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmissionRequest {
    @NotBlank(message = "problemId is required")
    private String problemId;

    @NotBlank(message = "code is required")
    private String code;

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

