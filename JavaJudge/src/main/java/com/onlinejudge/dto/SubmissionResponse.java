package com.onlinejudge.dto;

public class SubmissionResponse {
    private String verdict;
    private String message;
    private String expectedOutput;
    private String actualOutput;

    public SubmissionResponse() {
    }

    public SubmissionResponse(String verdict, String message) {
        this.verdict = verdict;
        this.message = message;
    }

    public SubmissionResponse(String verdict, String message, String expectedOutput, String actualOutput) {
        this.verdict = verdict;
        this.message = message;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }
}

