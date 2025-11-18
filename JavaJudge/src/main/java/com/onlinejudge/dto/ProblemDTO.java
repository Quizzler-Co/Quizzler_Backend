package com.onlinejudge.dto;

public class ProblemDTO {
    private String id;
    private String title;
    private String description;
    private String difficulty;
    
    public ProblemDTO() {
    }
    
    public ProblemDTO(String id, String title, String description, String difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    private String inputType;
    private String outputType;
    
    public String getInputType() {
        return inputType;
    }
    
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }
    
    public String getOutputType() {
        return outputType;
    }
    
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }
    
    private String methodName;
    private String parameterTypes;
    private String returnType;
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getParameterTypes() {
        return parameterTypes;
    }
    
    public void setParameterTypes(String parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    private String methodSignature; // Formatted method signature with parameter names for display
    
    public String getMethodSignature() {
        return methodSignature;
    }
    
    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }
}

