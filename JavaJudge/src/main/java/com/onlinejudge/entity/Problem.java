package com.onlinejudge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "problems")
public class Problem {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 5000)
    private String description;
    
    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column
    private String inputType = "String"; // String, int, long, double, etc. (deprecated, use methodSignature)
    
    @Column
    private String outputType = "String"; // String, int, long, double, etc. (deprecated, use methodSignature)
    
    // New: Full method signature support
    @Column
    private String methodName = "solve"; // Method name to call
    
    @Column(length = 1000)
    private String parameterTypes = "String"; // Comma-separated: "String" or "int,String" for multiple params
    
    @Column
    private String returnType = "String"; // Return type: String, int, long, double, etc.
    
    public Problem() {
    }
    
    public Problem(String id, String title, String description, String difficulty) {
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
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getInputType() {
        return inputType != null ? inputType : "String";
    }
    
    public void setInputType(String inputType) {
        this.inputType = inputType;
    }
    
    public String getOutputType() {
        return outputType != null ? outputType : "String";
    }
    
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }
    
    public String getMethodName() {
        return methodName != null ? methodName : "solve";
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getParameterTypes() {
        return parameterTypes != null ? parameterTypes : (inputType != null ? inputType : "String");
    }
    
    public void setParameterTypes(String parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    
    public String getReturnType() {
        return returnType != null ? returnType : (outputType != null ? outputType : "String");
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    // Helper method to get parameter types as array
    public String[] getParameterTypesArray() {
        String params = getParameterTypes();
        if (params == null || params.trim().isEmpty()) {
            return new String[]{"String"};
        }
        return params.split(",");
    }
}

