package com.onlinejudge.service;

import com.onlinejudge.dto.SubmissionRequest;
import com.onlinejudge.dto.SubmissionResponse;
import com.onlinejudge.entity.Problem;
import com.onlinejudge.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class JudgeService {

    private static final Logger logger = Logger.getLogger(JudgeService.class.getName());
    
    @Autowired
    private ProblemRepository problemRepository;
    
    // Use project-relative temp directory for better Docker compatibility on Windows
    private static final String SUBMISSION_TEMP_DIRECTORY = Paths.get(System.getProperty("user.dir"), "tmp", "jobs").toString();
    private static final String PROBLEMS_BASE_DIRECTORY = "judge/problems";

    public SubmissionResponse judge(SubmissionRequest request) {
        String submissionId = UUID.randomUUID().toString();
        Path submissionDirectory = Paths.get(SUBMISSION_TEMP_DIRECTORY, submissionId);

        try {
            // Get problem metadata to determine method signature
            Optional<Problem> problemOpt = problemRepository.findById(request.getProblemId());
            Problem problem;
            String methodName = "solve";
            String[] parameterTypes = new String[]{"String"};
            String returnType = "String";
                
            if (problemOpt.isPresent()) {
                problem = problemOpt.get();
                methodName = problem.getMethodName();
                parameterTypes = problem.getParameterTypesArray();
                returnType = problem.getReturnType();
            } else {
                // Fallback to default
                problem = null;
            }
            
            // Create submission directory - wrap in try-catch to prevent exceptions
            try {
                Files.createDirectories(submissionDirectory);
            } catch (Exception e) {
                logger.severe("Failed to create submission directory: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Failed to create submission directory: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }

            // Generate UserSolution.java with real method signature - wrap in try-catch
            String wrappedUserCode;
            try {
                wrappedUserCode = generateUserSolutionWithSignature(request.getCode(), methodName, parameterTypes, returnType);
            } catch (Exception e) {
                logger.severe("Failed to generate user solution code: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Failed to process user code: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
            
            Path userSolutionFilePath = submissionDirectory.resolve("UserSolution.java");
            try {
                Files.write(userSolutionFilePath, wrappedUserCode.getBytes());
            } catch (Exception e) {
                logger.severe("Failed to write UserSolution.java: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Failed to write solution file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }

            // Generate custom Runner.java for this problem's signature - wrap in try-catch
            String runnerCode;
            try {
                runnerCode = generateCustomRunner(methodName, parameterTypes, returnType);
            } catch (Exception e) {
                logger.severe("Failed to generate runner code: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Failed to generate runner code: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
            
            Path runnerFilePath = submissionDirectory.resolve("Runner.java");
            try {
                Files.write(runnerFilePath, runnerCode.getBytes());
            } catch (Exception e) {
                logger.severe("Failed to write Runner.java: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Failed to write runner file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }

            // Get problem test case
            String problemTestInput;
            String expectedTestOutput;
            try {
                problemTestInput = getProblemInput(request.getProblemId());
                expectedTestOutput = getProblemExpectedOutput(request.getProblemId());
            } catch (IOException e) {
                logger.severe("Error reading problem files for problemId: " + request.getProblemId());
                logger.severe("Stack trace: " + getStackTrace(e));
                return new SubmissionResponse("ERROR", 
                    "Problem not found or test case files are missing. Problem ID: " + request.getProblemId() + 
                    ". Error: " + (e.getMessage() != null ? e.getMessage() : "File not found"));
            }
            
            // Write input to a file for Docker to read - wrap in try-catch
            Path testInputFilePath = submissionDirectory.resolve("input.txt");
            try {
                Files.write(testInputFilePath, problemTestInput.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.severe("Failed to write input.txt: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Failed to write test input file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
            
            logger.info("Test input length: " + problemTestInput.length());
            logger.info("Test input content: [" + problemTestInput.replace("\n", "\\n").replace("\r", "\\r") + "]");
            logger.info("Expected output: [" + expectedTestOutput + "]");

            // Compile in Docker - treat all compilation issues (including exceptions) as compilation errors
            String compilationErrors = "";
            try {
                compilationErrors = compileInDocker(submissionId);
            } catch (IOException e) {
                logger.warning("IO Error during compilation: " + e.getMessage());
                compilationErrors = "Compilation failed due to I/O error: " + e.getMessage();
            } catch (InterruptedException e) {
                logger.warning("Compilation interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                compilationErrors = "Compilation was interrupted: " + e.getMessage();
            } catch (Exception e) {
                logger.warning("Unexpected error during compilation: " + e.getMessage());
                compilationErrors = "Compilation failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
            
            // Return compilation error as a successful response (not an exception)
            if (compilationErrors != null && !compilationErrors.trim().isEmpty()) {
                logger.info("Compilation error detected (returning as COMPILATION_ERROR response): " + compilationErrors);
                return new SubmissionResponse("COMPILATION_ERROR", compilationErrors);
            }

            // Run in Docker - treat all execution issues (including exceptions) as runtime errors
            ExecutionResult executionResult = null;
            try {
                logger.info("Running in Docker with input length: " + problemTestInput.length());
                executionResult = runInDocker(submissionId, problemTestInput);
                logger.info("Execution result - output: " + (executionResult.output != null ? executionResult.output.length() + " chars" : "null"));
                logger.info("Execution result - error: " + (executionResult.error != null && !executionResult.error.isEmpty() ? executionResult.error : "none"));
            } catch (IOException e) {
                logger.warning("IO Error during execution: " + e.getMessage());
                return new SubmissionResponse("RUNTIME_ERROR", 
                    "Execution failed due to I/O error: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.warning("Execution interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return new SubmissionResponse("RUNTIME_ERROR", 
                    "Execution was interrupted: " + e.getMessage());
            } catch (Exception e) {
                logger.warning("Unexpected error during execution: " + e.getMessage());
                return new SubmissionResponse("RUNTIME_ERROR", 
                    "Execution failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
            
            // Return runtime error as a successful response (not an exception)
            if (executionResult == null || executionResult.output == null) {
                String errorMessage = "Execution failed or timed out";
                if (executionResult != null && executionResult.error != null && !executionResult.error.isEmpty()) {
                    errorMessage += ": " + executionResult.error;
                }
                logger.info("Runtime error detected (returning as RUNTIME_ERROR response): " + errorMessage);
                return new SubmissionResponse("RUNTIME_ERROR", errorMessage);
            }
            String actualTestOutput = executionResult.output;

            // Compare outputs (handle numeric types) - wrap in try-catch
            String trimmedExpectedOutput = expectedTestOutput.trim();
            String trimmedActualOutput = actualTestOutput.trim();

            boolean outputsMatch;
            try {
                outputsMatch = compareOutputs(trimmedExpectedOutput, trimmedActualOutput, returnType);
            } catch (Exception e) {
                logger.severe("Error comparing outputs: " + e.getMessage());
                return new SubmissionResponse("ERROR", 
                    "Error comparing outputs: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }

            if (outputsMatch) {
                return new SubmissionResponse("ACCEPTED", "Solution is correct",
                        trimmedExpectedOutput, trimmedActualOutput);
            } else {
                return new SubmissionResponse("WRONG_ANSWER", "Output does not match",
                        trimmedExpectedOutput, trimmedActualOutput);
            }

        } catch (Exception e) {
            logger.severe("Unexpected error during submission: " + e.getMessage());
            logger.severe("Exception type: " + e.getClass().getName());
            logger.severe("Stack trace: " + getStackTrace(e));
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            // If it's a compilation or runtime error that wasn't caught, try to extract it
            if (errorMessage.contains("compilation") || errorMessage.contains("Compilation")) {
                return new SubmissionResponse("COMPILATION_ERROR", errorMessage);
            }
            if (errorMessage.contains("runtime") || errorMessage.contains("Runtime") || errorMessage.contains("Exception")) {
                return new SubmissionResponse("RUNTIME_ERROR", errorMessage);
            }
            return new SubmissionResponse("ERROR", "Internal error: " + errorMessage);
        } finally {
            // Cleanup
            try {
                deleteDirectory(submissionDirectory.toFile());
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    private String generateUserSolution(String userCode, String inputType, String outputType) {
        String trimmedCode = userCode.trim();
        
        // Check if user provided a full class with helper methods
        if (trimmedCode.contains("public class UserSolution") || 
            trimmedCode.contains("class UserSolution")) {
            // User provided full class - extract and preserve helper methods
            return extractAndPreserveClass(trimmedCode, inputType, outputType);
        }
        
        // Check if user provided full method signature (but not full class)
        // Check for: "public static solve(", "static solve(", or "public [returnType] solve("
        String methodPattern = "solve(";
        boolean hasMethodSignature = (trimmedCode.contains("public static") && trimmedCode.contains(methodPattern)) ||
                                     (trimmedCode.contains("static") && trimmedCode.contains(methodPattern)) ||
                                     (trimmedCode.contains("public") && trimmedCode.contains(methodPattern) && trimmedCode.contains("{"));
        
        if (hasMethodSignature) {
            // Extract only the code between braces (just the solve method body)
            trimmedCode = extractCodeBetweenBraces(trimmedCode);
        }
        
        // User provided only method body - wrap it
        // Check if user defined helper methods inside the method body
        // (nested methods or local functions - these are fine as-is)
        
        // Wrap user code in method body with proper indentation
        StringBuilder indentedUserCode = new StringBuilder();
        String[] codeLines = trimmedCode.split("\n");
        for (String codeLine : codeLines) {
            // Skip empty lines or preserve them
            if (codeLine.trim().isEmpty()) {
                indentedUserCode.append("\n");
            } else {
                indentedUserCode.append("        ").append(codeLine).append("\n");
            }
        }
        
        // Generate method signature based on types
        String methodSignature = generateMethodSignature(inputType, outputType);
        
        return "public class UserSolution {\n" +
               "    " + methodSignature + " {\n" +
               indentedUserCode.toString() +
               "    }\n" +
               "}\n";
    }
    
    private String extractAndPreserveClass(String fullClassCode, String inputType, String outputType) {
        // Find the class declaration
        int classStart = fullClassCode.indexOf("class UserSolution");
        if (classStart == -1) {
            classStart = fullClassCode.indexOf("class");
        }
        
        if (classStart == -1) {
            // Fallback to simple wrapping
            return generateUserSolution(fullClassCode, inputType, outputType);
        }
        
        // Find the opening brace of the class
        int classBraceStart = fullClassCode.indexOf("{", classStart);
        if (classBraceStart == -1) {
            return generateUserSolution(fullClassCode, inputType, outputType);
        }
        
        // Find the matching closing brace of the class
        int braceCount = 0;
        int classBraceEnd = classBraceStart;
        for (int i = classBraceStart; i < fullClassCode.length(); i++) {
            if (fullClassCode.charAt(i) == '{') {
                braceCount++;
            } else if (fullClassCode.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    classBraceEnd = i;
                    break;
                }
            }
        }
        
        // Extract class body (everything between class braces)
        String classBody = fullClassCode.substring(classBraceStart + 1, classBraceEnd).trim();
        
        // Check if solve method exists in the class
        String methodSignature = generateMethodSignature(inputType, outputType);
        String solveMethodPattern = "solve(";
        
        if (classBody.contains(solveMethodPattern)) {
            // User provided solve method - replace it with correct signature
            return replaceSolveMethodInClass(classBody, methodSignature, inputType, outputType);
        } else {
            // No solve method found - add it at the beginning
            StringBuilder result = new StringBuilder();
            result.append("public class UserSolution {\n");
            
            // Add user's helper methods first (preserve them)
            String[] classLines = classBody.split("\n");
            for (String line : classLines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    // Indent existing code
                    result.append("    ").append(line).append("\n");
                }
            }
            
            // Add solve method
            result.append("    ").append(methodSignature).append(" {\n");
            result.append("        // Write your solution here\n");
            result.append("    }\n");
            result.append("}\n");
            
            return result.toString();
        }
    }
    
    private String replaceSolveMethodInClass(String classBody, String correctMethodSignature, String inputType, String outputType) {
        // Find the solve method in the class body
        int solveStart = classBody.indexOf("solve(");
        if (solveStart == -1) {
            solveStart = classBody.indexOf("solve");
        }
        
        if (solveStart == -1) {
            // No solve method found, just wrap the class body
            return "public class UserSolution {\n" + 
                   indentClassBody(classBody) + 
                   "    " + correctMethodSignature + " {\n" +
                   "        // Write your solution here\n" +
                   "    }\n" +
                   "}\n";
        }
        
        // Find the method signature end (opening brace)
        int methodBraceStart = classBody.indexOf("{", solveStart);
        if (methodBraceStart == -1) {
            return "public class UserSolution {\n" + 
                   indentClassBody(classBody) + 
                   "    " + correctMethodSignature + " {\n" +
                   "        // Write your solution here\n" +
                   "    }\n" +
                   "}\n";
        }
        
        // Find matching closing brace for the solve method
        int braceCount = 0;
        int methodBraceEnd = methodBraceStart;
        for (int i = methodBraceStart; i < classBody.length(); i++) {
            if (classBody.charAt(i) == '{') {
                braceCount++;
            } else if (classBody.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    methodBraceEnd = i;
                    break;
                }
            }
        }
        
        // Extract solve method body
        String solveMethodBody = classBody.substring(methodBraceStart + 1, methodBraceEnd).trim();
        
        // Get everything before solve method (helper methods)
        String beforeSolve = classBody.substring(0, solveStart).trim();
        
        // Get everything after solve method (more helper methods)
        String afterSolve = classBody.substring(methodBraceEnd + 1).trim();
        
        // Check if there are non-static helper methods
        String allHelperMethods = beforeSolve + "\n" + afterSolve;
        boolean hasNonStaticHelpers = hasNonStaticMethods(allHelperMethods);
        
        // Get list of non-static method names
        java.util.Set<String> nonStaticMethodNames = new java.util.HashSet<>();
        if (hasNonStaticHelpers) {
            nonStaticMethodNames = extractNonStaticMethodNames(allHelperMethods);
        }
        
        // Reconstruct class with correct solve method signature
        StringBuilder result = new StringBuilder();
        result.append("public class UserSolution {\n");
        
        // Add helper methods before solve
        if (!beforeSolve.isEmpty()) {
            result.append(indentClassBody(beforeSolve));
        }
        
        // Add solve method with correct signature
        result.append("    ").append(correctMethodSignature).append(" {\n");
        
        // If there are non-static helper methods, create an instance to call them
        if (hasNonStaticHelpers) {
            result.append("        UserSolution solution = new UserSolution();\n");
        }
        
        // Process solve method body and prefix non-static method calls
        String processedBody = processMethodBodyForNonStaticCalls(solveMethodBody, nonStaticMethodNames);
        String[] bodyLines = processedBody.split("\n");
        for (String line : bodyLines) {
            result.append("        ").append(line).append("\n");
        }
        result.append("    }\n");
        
        // Add helper methods after solve
        if (!afterSolve.isEmpty()) {
            result.append(indentClassBody(afterSolve));
        }
        
        result.append("}\n");
        
        return result.toString();
    }
    
    private boolean hasNonStaticMethods(String code) {
        // Check for methods that don't have "static" keyword
        String[] lines = code.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // Look for method declarations without "static"
            if ((trimmed.contains("private") || trimmed.contains("public") || trimmed.contains("protected")) &&
                trimmed.contains("(") && 
                !trimmed.contains("static") &&
                !trimmed.contains("solve(")) {
                return true;
            }
        }
        return false;
    }
    
    private java.util.Set<String> extractNonStaticMethodNames(String code) {
        java.util.Set<String> methodNames = new java.util.HashSet<>();
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Check if this is a non-static method declaration
            if ((trimmed.contains("private") || trimmed.contains("public") || trimmed.contains("protected")) &&
                trimmed.contains("(") && 
                !trimmed.contains("static") &&
                !trimmed.contains("solve(")) {
                
                // Extract method name
                int parenIndex = trimmed.indexOf("(");
                if (parenIndex > 0) {
                    String beforeParen = trimmed.substring(0, parenIndex).trim();
                    // Get the method name (last identifier before parentheses)
                    String[] parts = beforeParen.split("\\s+");
                    if (parts.length > 0) {
                        String methodName = parts[parts.length - 1];
                        // Remove any type parameters or array brackets
                        methodName = methodName.replaceAll("\\[\\]", "").trim();
                        if (!methodName.isEmpty()) {
                            methodNames.add(methodName);
                        }
                    }
                }
            }
        }
        
        return methodNames;
    }
    
    private String processMethodBodyForNonStaticCalls(String methodBody, java.util.Set<String> nonStaticMethodNames) {
        if (nonStaticMethodNames.isEmpty()) {
            return methodBody;
        }
        
        // Process each line to find and prefix non-static method calls
        String[] lines = methodBody.split("\n");
        StringBuilder processed = new StringBuilder();
        
        for (String line : lines) {
            String processedLine = prefixNonStaticMethodCalls(line, nonStaticMethodNames);
            processed.append(processedLine);
            if (processed.length() > 0 && !processedLine.endsWith("\n")) {
                processed.append("\n");
            }
        }
        
        return processed.toString();
    }
    
    private String prefixNonStaticMethodCalls(String line, java.util.Set<String> nonStaticMethodNames) {
        // Skip if already has solution. prefix
        if (line.trim().startsWith("solution.")) {
            return line;
        }
        
        // Find all method calls in the line
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        
        // Use regex to find method calls: methodName(
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
        java.util.regex.Matcher matcher = pattern.matcher(line);
        
        while (matcher.find()) {
            String methodName = matcher.group(1);
            
            // Check if this is a non-static method we need to prefix
            if (nonStaticMethodNames.contains(methodName)) {
                // Check if it's not already prefixed
                int start = matcher.start(1);
                
                // Check if there's already a dot or solution. before this
                String beforeCall = line.substring(0, start).trim();
                if (!beforeCall.endsWith(".") && !beforeCall.endsWith("solution")) {
                    // Add solution. prefix
                    result.append(line.substring(lastIndex, start));
                    result.append("solution.");
                    lastIndex = start;
                }
            }
        }
        
        // Add remaining part of line
        result.append(line.substring(lastIndex));
        
        return result.toString();
    }
    
    private String indentClassBody(String classBody) {
        StringBuilder indented = new StringBuilder();
        String[] lines = classBody.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                indented.append("\n");
            } else {
                // Preserve existing indentation, but ensure at least one level
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    indented.append("    ").append(line).append("\n");
                }
            }
        }
        return indented.toString();
    }
    
    private String generateMethodSignature(String inputType, String outputType) {
        // Convert type names to Java types
        String javaInputType = convertToJavaType(inputType);
        String javaOutputType = convertToJavaType(outputType);
        
        return "public static " + javaOutputType + " solve(" + javaInputType + " input)";
    }
    
    private String generateMethodSignature(String methodName, String[] parameterTypes, String returnType) {
        // Convert type names to Java types
        String javaReturnType = convertToJavaType(returnType);
        
        StringBuilder signature = new StringBuilder();
        signature.append("public static ").append(javaReturnType).append(" ").append(methodName).append("(");
        
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) signature.append(", ");
            String javaType = convertToJavaType(parameterTypes[i].trim());
            signature.append(javaType).append(" param").append(i + 1);
        }
        
        signature.append(")");
        return signature.toString();
    }
    
    private String generateUserSolutionWithSignature(String userCode, String methodName, String[] parameterTypes, String returnType) {
        String trimmedCode = userCode.trim();
        
        // Check if user provided a full class with helper methods
        if (trimmedCode.contains("public class UserSolution") || 
            trimmedCode.contains("class UserSolution")) {
            // User provided full class - extract and preserve helper methods
            return extractAndPreserveClassWithSignature(trimmedCode, methodName, parameterTypes, returnType);
        }
        
        // Check if user provided full method signature (but not full class)
        // Check for: "public static methodName(", "static methodName(", or "public [returnType] methodName("
        String methodPattern = methodName + "(";
        boolean hasMethodSignature = (trimmedCode.contains("public static") && trimmedCode.contains(methodPattern)) ||
                                     (trimmedCode.contains("static") && trimmedCode.contains(methodPattern)) ||
                                     (trimmedCode.contains("public") && trimmedCode.contains(methodPattern) && trimmedCode.contains("{"));
        
        String[] userParamNames = null;
        if (hasMethodSignature) {
            // Extract parameter names from user's signature
            userParamNames = extractParameterNames(trimmedCode, methodName);
            // Extract only the code between braces (just the method body)
            trimmedCode = extractCodeBetweenBraces(trimmedCode);
            // Replace user's parameter names with generated ones in the body
            if (userParamNames != null && userParamNames.length > 0) {
                for (int i = 0; i < userParamNames.length && i < parameterTypes.length; i++) {
                    String userParamName = userParamNames[i];
                    String generatedParamName = "param" + (i + 1);
                    if (userParamName != null && !userParamName.equals(generatedParamName)) {
                        // Replace parameter name in the body (word boundary to avoid partial matches)
                        trimmedCode = trimmedCode.replaceAll("\\b" + userParamName + "\\b", generatedParamName);
                    }
                }
            }
        }
        
        // Wrap user code in method body with proper indentation
        StringBuilder indentedUserCode = new StringBuilder();
        String[] codeLines = trimmedCode.split("\n");
        for (String codeLine : codeLines) {
            if (codeLine.trim().isEmpty()) {
                indentedUserCode.append("\n");
            } else {
                indentedUserCode.append("        ").append(codeLine).append("\n");
            }
        }
        
        // Generate method signature based on real signature
        String methodSignature = generateMethodSignature(methodName, parameterTypes, returnType);
        
        return "public class UserSolution {\n" +
               "    " + methodSignature + " {\n" +
               indentedUserCode.toString() +
               "    }\n" +
               "}\n";
    }
    
    private String extractAndPreserveClassWithSignature(String fullClassCode, String methodName, String[] parameterTypes, String returnType) {
        // Similar to extractAndPreserveClass but uses real signature
        int classStart = fullClassCode.indexOf("class UserSolution");
        if (classStart == -1) {
            classStart = fullClassCode.indexOf("class");
        }
        
        if (classStart == -1) {
            return generateUserSolutionWithSignature(fullClassCode, methodName, parameterTypes, returnType);
        }
        
        int classBraceStart = fullClassCode.indexOf("{", classStart);
        if (classBraceStart == -1) {
            return generateUserSolutionWithSignature(fullClassCode, methodName, parameterTypes, returnType);
        }
        
        int braceCount = 0;
        int classBraceEnd = classBraceStart;
        for (int i = classBraceStart; i < fullClassCode.length(); i++) {
            if (fullClassCode.charAt(i) == '{') {
                braceCount++;
            } else if (fullClassCode.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    classBraceEnd = i;
                    break;
                }
            }
        }
        
        String classBody = fullClassCode.substring(classBraceStart + 1, classBraceEnd).trim();
        String methodSignature = generateMethodSignature(methodName, parameterTypes, returnType);
        String methodPattern = methodName + "(";
        
        if (classBody.contains(methodPattern)) {
            return replaceMethodInClass(classBody, methodSignature, methodName, parameterTypes, returnType);
        } else {
            StringBuilder result = new StringBuilder();
            result.append("public class UserSolution {\n");
            
            String[] classLines = classBody.split("\n");
            for (String line : classLines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    result.append("    ").append(line).append("\n");
                }
            }
            
            result.append("    ").append(methodSignature).append(" {\n");
            result.append("        // Write your solution here\n");
            result.append("    }\n");
            result.append("}\n");
            
            return result.toString();
        }
    }
    
    private String replaceMethodInClass(String classBody, String correctMethodSignature, String methodName, String[] parameterTypes, String returnType) {
        // Similar to replaceSolveMethodInClass but uses methodName instead of "solve"
        int methodStart = classBody.indexOf(methodName + "(");
        if (methodStart == -1) {
            methodStart = classBody.indexOf(methodName);
        }
        
        if (methodStart == -1) {
            return "public class UserSolution {\n" + 
                   indentClassBody(classBody) + 
                   "    " + correctMethodSignature + " {\n" +
                   "        // Write your solution here\n" +
                   "    }\n" +
                   "}\n";
        }
        
        int methodBraceStart = classBody.indexOf("{", methodStart);
        if (methodBraceStart == -1) {
            return "public class UserSolution {\n" + 
                   indentClassBody(classBody) + 
                   "    " + correctMethodSignature + " {\n" +
                   "        // Write your solution here\n" +
                   "    }\n" +
                   "}\n";
        }
        
        int braceCount = 0;
        int methodBraceEnd = methodBraceStart;
        for (int i = methodBraceStart; i < classBody.length(); i++) {
            if (classBody.charAt(i) == '{') {
                braceCount++;
            } else if (classBody.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    methodBraceEnd = i;
                    break;
                }
            }
        }
        
        String methodBody = classBody.substring(methodBraceStart + 1, methodBraceEnd).trim();
        String beforeMethod = classBody.substring(0, methodStart).trim();
        String afterMethod = classBody.substring(methodBraceEnd + 1).trim();
        
        String allHelperMethods = beforeMethod + "\n" + afterMethod;
        boolean hasNonStaticHelpers = hasNonStaticMethods(allHelperMethods);
        java.util.Set<String> nonStaticMethodNames = new java.util.HashSet<>();
        if (hasNonStaticHelpers) {
            nonStaticMethodNames = extractNonStaticMethodNames(allHelperMethods);
        }
        
        StringBuilder result = new StringBuilder();
        result.append("public class UserSolution {\n");
        
        if (!beforeMethod.isEmpty()) {
            result.append(indentClassBody(beforeMethod));
        }
        
        result.append("    ").append(correctMethodSignature).append(" {\n");
        
        if (hasNonStaticHelpers) {
            result.append("        UserSolution solution = new UserSolution();\n");
        }
        
        String processedBody = processMethodBodyForNonStaticCalls(methodBody, nonStaticMethodNames);
        String[] bodyLines = processedBody.split("\n");
        for (String line : bodyLines) {
            result.append("        ").append(line).append("\n");
        }
        result.append("    }\n");
        
        if (!afterMethod.isEmpty()) {
            result.append(indentClassBody(afterMethod));
        }
        
        result.append("}\n");
        
        return result.toString();
    }
    
    private String generateCustomRunner(String methodName, String[] parameterTypes, String returnType) {
        StringBuilder runner = new StringBuilder();
        runner.append("import java.util.Scanner;\n\n");
        runner.append("public class Runner {\n");
        runner.append("    public static void main(String[] args) {\n");
        runner.append("        Scanner scanner = new Scanner(System.in);\n");
        runner.append("        StringBuilder input = new StringBuilder();\n");
        runner.append("        \n");
        runner.append("        while (scanner.hasNextLine()) {\n");
        runner.append("            input.append(scanner.nextLine());\n");
        runner.append("            if (scanner.hasNextLine()) {\n");
        runner.append("                input.append(\"\\n\");\n");
        runner.append("            }\n");
        runner.append("        }\n");
        runner.append("        scanner.close();\n");
        runner.append("        \n");
        runner.append("        String inputStr = input.toString();\n");
        runner.append("        \n");
        
        // Parse input based on parameter types
        runner.append("        // Parse input parameters\n");
        for (int i = 0; i < parameterTypes.length; i++) {
            String paramType = convertToJavaType(parameterTypes[i].trim());
            runner.append("        ").append(paramType).append(" param").append(i + 1).append(" = ");
            runner.append(generateParameterParser(paramType, i, parameterTypes.length));
            runner.append(";\n");
        }
        
        // Call the method
        runner.append("        \n");
        runner.append("        // Call user's method\n");
        String javaReturnType = convertToJavaType(returnType);
        runner.append("        ").append(javaReturnType).append(" result = UserSolution.").append(methodName).append("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) runner.append(", ");
            runner.append("param").append(i + 1);
        }
        runner.append(");\n");
        runner.append("        \n");
        
        // Output result
        runner.append("        // Output result\n");
        if (javaReturnType.endsWith("[]")) {
            // Array return type - convert to string
            runner.append("        System.out.print(arrayToString(result));\n");
        } else {
            runner.append("        System.out.print(result);\n");
        }
        
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String[] parseInputLines(String input) {\n");
        runner.append("        return input.trim().split(\"\\\\n\");\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String[] parseTokens(String line) {\n");
        runner.append("        if (line == null || line.trim().isEmpty()) return new String[0];\n");
        runner.append("        return line.trim().split(\"\\\\s+\");\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    // Array parsing helpers\n");
        runner.append("    private static int[] parseIntArray(String input) {\n");
        runner.append("        if (input == null || input.trim().isEmpty()) return new int[0];\n");
        runner.append("        String[] tokens = input.trim().split(\"\\\\s+\");\n");
        runner.append("        int[] result = new int[tokens.length];\n");
        runner.append("        for (int i = 0; i < tokens.length; i++) {\n");
        runner.append("            result[i] = Integer.parseInt(tokens[i]);\n");
        runner.append("        }\n");
        runner.append("        return result;\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static long[] parseLongArray(String input) {\n");
        runner.append("        if (input == null || input.trim().isEmpty()) return new long[0];\n");
        runner.append("        String[] tokens = input.trim().split(\"\\\\s+\");\n");
        runner.append("        long[] result = new long[tokens.length];\n");
        runner.append("        for (int i = 0; i < tokens.length; i++) {\n");
        runner.append("            result[i] = Long.parseLong(tokens[i]);\n");
        runner.append("        }\n");
        runner.append("        return result;\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static double[] parseDoubleArray(String input) {\n");
        runner.append("        if (input == null || input.trim().isEmpty()) return new double[0];\n");
        runner.append("        String[] tokens = input.trim().split(\"\\\\s+\");\n");
        runner.append("        double[] result = new double[tokens.length];\n");
        runner.append("        for (int i = 0; i < tokens.length; i++) {\n");
        runner.append("            result[i] = Double.parseDouble(tokens[i]);\n");
        runner.append("        }\n");
        runner.append("        return result;\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static float[] parseFloatArray(String input) {\n");
        runner.append("        if (input == null || input.trim().isEmpty()) return new float[0];\n");
        runner.append("        String[] tokens = input.trim().split(\"\\\\s+\");\n");
        runner.append("        float[] result = new float[tokens.length];\n");
        runner.append("        for (int i = 0; i < tokens.length; i++) {\n");
        runner.append("            result[i] = Float.parseFloat(tokens[i]);\n");
        runner.append("        }\n");
        runner.append("        return result;\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String[] parseStringArray(String input) {\n");
        runner.append("        if (input == null || input.trim().isEmpty()) return new String[0];\n");
        runner.append("        return input.trim().split(\"\\\\s+\");\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    // Array output helpers\n");
        runner.append("    private static String arrayToString(int[] arr) {\n");
        runner.append("        if (arr == null || arr.length == 0) return \"\";\n");
        runner.append("        StringBuilder sb = new StringBuilder();\n");
        runner.append("        for (int i = 0; i < arr.length; i++) {\n");
        runner.append("            if (i > 0) sb.append(\" \");\n");
        runner.append("            sb.append(arr[i]);\n");
        runner.append("        }\n");
        runner.append("        return sb.toString();\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String arrayToString(long[] arr) {\n");
        runner.append("        if (arr == null || arr.length == 0) return \"\";\n");
        runner.append("        StringBuilder sb = new StringBuilder();\n");
        runner.append("        for (int i = 0; i < arr.length; i++) {\n");
        runner.append("            if (i > 0) sb.append(\" \");\n");
        runner.append("            sb.append(arr[i]);\n");
        runner.append("        }\n");
        runner.append("        return sb.toString();\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String arrayToString(double[] arr) {\n");
        runner.append("        if (arr == null || arr.length == 0) return \"\";\n");
        runner.append("        StringBuilder sb = new StringBuilder();\n");
        runner.append("        for (int i = 0; i < arr.length; i++) {\n");
        runner.append("            if (i > 0) sb.append(\" \");\n");
        runner.append("            sb.append(arr[i]);\n");
        runner.append("        }\n");
        runner.append("        return sb.toString();\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String arrayToString(float[] arr) {\n");
        runner.append("        if (arr == null || arr.length == 0) return \"\";\n");
        runner.append("        StringBuilder sb = new StringBuilder();\n");
        runner.append("        for (int i = 0; i < arr.length; i++) {\n");
        runner.append("            if (i > 0) sb.append(\" \");\n");
        runner.append("            sb.append(arr[i]);\n");
        runner.append("        }\n");
        runner.append("        return sb.toString();\n");
        runner.append("    }\n");
        runner.append("    \n");
        runner.append("    private static String arrayToString(String[] arr) {\n");
        runner.append("        if (arr == null || arr.length == 0) return \"\";\n");
        runner.append("        return String.join(\" \", arr);\n");
        runner.append("    }\n");
        runner.append("}\n");
        
        return runner.toString();
    }
    
    private String generateParameterParser(String javaType, int paramIndex, int totalParams) {
        // Handle array types
        if (javaType.endsWith("[]")) {
            String baseType = javaType.substring(0, javaType.length() - 2);
            if (totalParams == 1) {
                // Single parameter - parse entire input as array
                return generateArrayParser(baseType, "inputStr");
            } else {
                // Multiple parameters - parse from specific line
                return generateArrayParser(baseType, "parseInputLines(inputStr)[" + paramIndex + "]");
            }
        }
        
        // For single parameter, parse from first line
        // For multiple parameters, parse from different lines or tokens
        if (totalParams == 1) {
            // Single parameter - parse entire input
            if (javaType.equals("int")) {
                return "Integer.parseInt(inputStr.trim())";
            } else if (javaType.equals("long")) {
                return "Long.parseLong(inputStr.trim())";
            } else if (javaType.equals("double")) {
                return "Double.parseDouble(inputStr.trim())";
            } else if (javaType.equals("float")) {
                return "Float.parseFloat(inputStr.trim())";
            } else {
                return "inputStr";
            }
        } else {
            // Multiple parameters - parse from lines or tokens
            if (javaType.equals("int")) {
                return "Integer.parseInt(parseInputLines(inputStr)[" + paramIndex + "].trim())";
            } else if (javaType.equals("long")) {
                return "Long.parseLong(parseInputLines(inputStr)[" + paramIndex + "].trim())";
            } else if (javaType.equals("double")) {
                return "Double.parseDouble(parseInputLines(inputStr)[" + paramIndex + "].trim())";
            } else if (javaType.equals("float")) {
                return "Float.parseFloat(parseInputLines(inputStr)[" + paramIndex + "].trim())";
            } else {
                return "parseInputLines(inputStr)[" + paramIndex + "]";
            }
        }
    }
    
    private String generateArrayParser(String baseType, String inputSource) {
        // Generate code to parse space-separated or newline-separated values into array
        StringBuilder parser = new StringBuilder();
        
        if (baseType.equals("int")) {
            parser.append("parseIntArray(").append(inputSource).append(")");
        } else if (baseType.equals("long")) {
            parser.append("parseLongArray(").append(inputSource).append(")");
        } else if (baseType.equals("double")) {
            parser.append("parseDoubleArray(").append(inputSource).append(")");
        } else if (baseType.equals("float")) {
            parser.append("parseFloatArray(").append(inputSource).append(")");
        } else {
            // String array
            parser.append("parseStringArray(").append(inputSource).append(")");
        }
        
        return parser.toString();
    }
    
    private String convertToJavaType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "String";
        }
        
        String normalizedType = type.trim().toLowerCase();
        
        // Check for array types first (e.g., "int[]", "int[]", "String[]")
        if (normalizedType.endsWith("[]")) {
            String baseType = normalizedType.substring(0, normalizedType.length() - 2).trim();
            String javaBaseType = convertBaseTypeToJava(baseType);
            return javaBaseType + "[]";
        }
        
        // Handle primitive and object types
        return convertBaseTypeToJava(normalizedType);
    }
    
    private String convertBaseTypeToJava(String normalizedType) {
        switch (normalizedType) {
            case "int":
            case "integer":
                return "int";
            case "long":
                return "long";
            case "double":
                return "double";
            case "float":
                return "float";
            case "string":
            default:
                return "String";
        }
    }
    
    private String generateRunnerTemplate(String inputType, String outputType) {
        String javaInputType = convertToJavaType(inputType);
        String javaOutputType = convertToJavaType(outputType);
        
        StringBuilder template = new StringBuilder();
        template.append("import java.util.Scanner;\n\n");
        template.append("public class RunnerTemplate {\n");
        template.append("    public static void main(String[] args) {\n");
        template.append("        Scanner scanner = new Scanner(System.in);\n");
        template.append("        StringBuilder input = new StringBuilder();\n");
        template.append("        \n");
        template.append("        while (scanner.hasNextLine()) {\n");
        template.append("            input.append(scanner.nextLine());\n");
        template.append("            if (scanner.hasNextLine()) {\n");
        template.append("                input.append(\"\\n\");\n");
        template.append("            }\n");
        template.append("        }\n");
        template.append("        scanner.close();\n");
        template.append("        \n");
        
        // Parse input based on type
        if (javaInputType.equals("int")) {
            template.append("        int parsedInput = Integer.parseInt(input.toString().trim());\n");
            template.append("        ").append(javaOutputType).append(" result = UserSolution.solve(parsedInput);\n");
        } else if (javaInputType.equals("long")) {
            template.append("        long parsedInput = Long.parseLong(input.toString().trim());\n");
            template.append("        ").append(javaOutputType).append(" result = UserSolution.solve(parsedInput);\n");
        } else if (javaInputType.equals("double")) {
            template.append("        double parsedInput = Double.parseDouble(input.toString().trim());\n");
            template.append("        ").append(javaOutputType).append(" result = UserSolution.solve(parsedInput);\n");
        } else if (javaInputType.equals("float")) {
            template.append("        float parsedInput = Float.parseFloat(input.toString().trim());\n");
            template.append("        ").append(javaOutputType).append(" result = UserSolution.solve(parsedInput);\n");
        } else {
            template.append("        ").append(javaOutputType).append(" result = UserSolution.solve(input.toString());\n");
        }
        
        // Output result based on type
        if (javaOutputType.equals("String")) {
            template.append("        System.out.print(result);\n");
        } else {
            template.append("        System.out.print(result);\n");
        }
        
        template.append("    }\n");
        template.append("}\n");
        
        return template.toString();
    }
    
    private boolean compareOutputs(String expected, String actual, String outputType) {
        String normalizedType = outputType != null ? outputType.trim().toLowerCase() : "string";
        
        // For numeric types, compare as numbers (ignore whitespace)
        if (normalizedType.equals("int") || normalizedType.equals("integer")) {
            try {
                int expectedInt = Integer.parseInt(expected.trim());
                int actualInt = Integer.parseInt(actual.trim());
                return expectedInt == actualInt;
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                return expected.equals(actual);
            }
        } else if (normalizedType.equals("long")) {
            try {
                long expectedLong = Long.parseLong(expected.trim());
                long actualLong = Long.parseLong(actual.trim());
                return expectedLong == actualLong;
            } catch (NumberFormatException e) {
                return expected.equals(actual);
            }
        } else if (normalizedType.equals("double")) {
            try {
                double expectedDouble = Double.parseDouble(expected.trim());
                double actualDouble = Double.parseDouble(actual.trim());
                // Use epsilon for floating point comparison
                return Math.abs(expectedDouble - actualDouble) < 1e-9;
            } catch (NumberFormatException e) {
                return expected.equals(actual);
            }
        } else if (normalizedType.equals("float")) {
            try {
                float expectedFloat = Float.parseFloat(expected.trim());
                float actualFloat = Float.parseFloat(actual.trim());
                return Math.abs(expectedFloat - actualFloat) < 1e-6;
            } catch (NumberFormatException e) {
                return expected.equals(actual);
            }
        }
        
        // Default: string comparison
        return expected.equals(actual);
    }
    
    private String extractCodeBetweenBraces(String code) {
        // Find the first opening brace
        int braceStart = code.indexOf("{");
        if (braceStart == -1) {
            return code; // No braces found, return as-is
        }
        
        // Find the matching closing brace
        int braceCount = 0;
        int braceEnd = braceStart;
        for (int i = braceStart; i < code.length(); i++) {
            if (code.charAt(i) == '{') {
                braceCount++;
            } else if (code.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    braceEnd = i;
                    break;
                }
            }
        }
        
        // Extract content between braces
        if (braceEnd > braceStart) {
            return code.substring(braceStart + 1, braceEnd).trim();
        }
        
        return code;
    }
    
    private String[] extractParameterNames(String code, String methodName) {
        // Find the method signature
        int methodStart = code.indexOf(methodName + "(");
        if (methodStart == -1) {
            return null;
        }
        
        // Find the opening parenthesis
        int paramStart = methodStart + methodName.length() + 1;
        int paramEnd = code.indexOf(")", paramStart);
        if (paramEnd == -1) {
            return null;
        }
        
        // Extract parameter list
        String paramList = code.substring(paramStart, paramEnd).trim();
        if (paramList.isEmpty()) {
            return new String[0];
        }
        
        // Split by comma and extract parameter names
        String[] params = paramList.split(",");
        String[] paramNames = new String[params.length];
        
        for (int i = 0; i < params.length; i++) {
            String param = params[i].trim();
            // Parameter format: "Type name" or "Type[] name" or "final Type name"
            // Extract the last word as the parameter name
            String[] parts = param.split("\\s+");
            if (parts.length > 0) {
                // Get the last part (parameter name)
                paramNames[i] = parts[parts.length - 1];
            } else {
                paramNames[i] = null;
            }
        }
        
        return paramNames;
    }
    

    private String getProblemInput(String problemId) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(
                PROBLEMS_BASE_DIRECTORY + "/" + problemId + "/input.txt");
        try (InputStream inputStream = classPathResource.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder fileContent = new StringBuilder();
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                fileContent.append(inputLine).append("\n");
            }
            return fileContent.toString();
        }
    }

    private String getProblemExpectedOutput(String problemId) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(
                PROBLEMS_BASE_DIRECTORY + "/" + problemId + "/expected.txt");
        try (InputStream inputStream = classPathResource.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder fileContent = new StringBuilder();
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                fileContent.append(inputLine).append("\n");
            }
            return fileContent.toString();
        }
    }

    private String compileInDocker(String submissionId) throws IOException, InterruptedException {
        Path jobDirectoryPath = Paths.get(SUBMISSION_TEMP_DIRECTORY, submissionId).toAbsolutePath();
        String dockerVolumeMount = formatDockerVolumeMount(jobDirectoryPath.toString());
        
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", dockerVolumeMount,
                "eclipse-temurin:17-jdk",
                "sh", "-c", "cd /workspace && javac *.java 2>&1"
        );

        Process compilationProcess = processBuilder.start();
        StringBuilder compilationErrors = new StringBuilder();
        
        // Read both stdout and stderr (javac outputs errors to stderr, but we redirect with 2>&1)
        Thread stdoutReaderThread = new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(compilationProcess.getInputStream()))) {
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    synchronized (compilationErrors) {
                        compilationErrors.append(outputLine).append("\n");
                    }
                }
            } catch (IOException e) {
                // Ignore
            }
        });
        
        Thread stderrReaderThread = new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(compilationProcess.getErrorStream()))) {
                String errorLine;
                while ((errorLine = bufferedReader.readLine()) != null) {
                    synchronized (compilationErrors) {
                        compilationErrors.append(errorLine).append("\n");
                    }
                }
            } catch (IOException e) {
                // Ignore
            }
        });
        
        stdoutReaderThread.start();
        stderrReaderThread.start();
        
        // Wait for process with timeout (10 seconds for compilation)
        boolean processCompleted = compilationProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
        if (!processCompleted) {
            compilationProcess.destroyForcibly();
            stdoutReaderThread.join(1000);
            stderrReaderThread.join(1000);
            String timeoutError = "Compilation timeout (exceeded 10 seconds)";
            if (compilationErrors.length() > 0) {
                timeoutError += "\nPartial compilation output:\n" + compilationErrors.toString();
            }
            return timeoutError;
        }
        
        stdoutReaderThread.join(1000);
        stderrReaderThread.join(1000);

        int processExitCode = compilationProcess.exitValue();
        if (processExitCode != 0) {
            return compilationErrors.toString();
        }
        return "";
    }

    private static class ExecutionResult {
        String output;
        String error;
        ExecutionResult(String output, String error) {
            this.output = output;
            this.error = error;
        }
    }
    
    private ExecutionResult runInDocker(String submissionId, String testInput) throws IOException, InterruptedException {
        Path jobDirectoryPath = Paths.get(SUBMISSION_TEMP_DIRECTORY, submissionId).toAbsolutePath();
        String dockerVolumeMount = formatDockerVolumeMount(jobDirectoryPath.toString());
        
        logger.info("Docker volume mount: " + dockerVolumeMount);
        logger.info("Job path: " + jobDirectoryPath.toString());
        
        // Use input file instead of stdin for better reliability on Windows
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", dockerVolumeMount,
                "eclipse-temurin:17-jdk",
                "sh", "-c", "cd /workspace && java Runner < input.txt"
        );
        
        logger.info("Docker command: " + String.join(" ", processBuilder.command()));

        Process executionProcess = processBuilder.start();
        
        // Use a separate thread to read output to avoid deadlock
        StringBuilder programOutput = new StringBuilder();
        StringBuilder stderrOutput = new StringBuilder();
        
        Thread stdoutReaderThread = new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(executionProcess.getInputStream()))) {
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    programOutput.append(outputLine).append("\n");
                }
            } catch (IOException e) {
                // Ignore
            }
        });
        
        Thread stderrReaderThread = new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(executionProcess.getErrorStream()))) {
                String errorLine;
                while ((errorLine = bufferedReader.readLine()) != null) {
                    stderrOutput.append(errorLine).append("\n");
                }
            } catch (IOException e) {
                // Ignore
            }
        });
        
        stdoutReaderThread.start();
        stderrReaderThread.start();
        logger.info("Output threads started, waiting for process");
        
        // Wait for process with timeout (5 seconds)
        boolean processCompleted = executionProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
        if (!processCompleted) {
            executionProcess.destroyForcibly();
            stdoutReaderThread.join(1000);
            stderrReaderThread.join(1000);
            return new ExecutionResult(null, "Execution timeout");
        }
        
        stdoutReaderThread.join(1000);
        stderrReaderThread.join(1000);
        
        logger.info("Output captured: [" + programOutput.toString().replace("\n", "\\n") + "]");
        logger.info("Error captured: [" + stderrOutput.toString().replace("\n", "\\n") + "]");
        
        int processExitCode = executionProcess.exitValue();
        logger.info("Process exit code: " + processExitCode);
        
        if (processExitCode != 0) {
            return new ExecutionResult(null, stderrOutput.toString()); // Runtime error
        }

        return new ExecutionResult(programOutput.toString(), stderrOutput.toString());
    }

    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    private String formatDockerVolumeMount(String windowsPath) {
        // Convert Windows path to Docker volume mount format
        // For Windows with Docker Desktop, we need to use the Windows path format
        // D:\Quizzler\... -> D:/Quizzler/...
        String normalizedPath = windowsPath.replace("\\", "/");
        
        // For Docker Desktop on Windows, paths need to be accessible
        // Try to use the path as-is first, Docker Desktop should handle it
        // If this doesn't work, we might need to convert to WSL path format
        return normalizedPath + ":/workspace:rw";
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}

