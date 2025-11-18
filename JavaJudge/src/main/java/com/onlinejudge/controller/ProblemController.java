package com.onlinejudge.controller;

import com.onlinejudge.dto.ProblemDTO;
import com.onlinejudge.entity.Problem;
import com.onlinejudge.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProblemController {

    @Autowired
    private ProblemRepository problemRepository;

    @GetMapping("/problems")
    public ResponseEntity<List<ProblemDTO>> getAllProblems(
            @RequestParam(required = false) String difficulty) {
        
        List<Problem> problems;
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            problems = problemRepository.findByActiveTrueAndDifficulty(difficulty.toUpperCase());
        } else {
            problems = problemRepository.findByActiveTrue();
        }
        
               List<ProblemDTO> problemDTOs = problems.stream()
                       .map(p -> {
                           ProblemDTO dto = new ProblemDTO(p.getId(), p.getTitle(), p.getDescription(), p.getDifficulty());
                           dto.setInputType(p.getInputType());
                           dto.setOutputType(p.getOutputType());
                           dto.setMethodName(p.getMethodName());
                           dto.setParameterTypes(p.getParameterTypes());
                           dto.setReturnType(p.getReturnType());
                           dto.setMethodSignature(generateMethodSignature(p));
                           return dto;
                       })
                       .collect(Collectors.toList());
        
        return ResponseEntity.ok(problemDTOs);
    }

    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ProblemDTO> getProblem(@PathVariable String problemId) {
           return problemRepository.findById(problemId)
                       .map(p -> {
                           ProblemDTO dto = new ProblemDTO(p.getId(), p.getTitle(), p.getDescription(), p.getDifficulty());
                           dto.setInputType(p.getInputType());
                           dto.setOutputType(p.getOutputType());
                           dto.setMethodName(p.getMethodName());
                           dto.setParameterTypes(p.getParameterTypes());
                           dto.setReturnType(p.getReturnType());
                           dto.setMethodSignature(generateMethodSignature(p));
                           return ResponseEntity.ok(dto);
                       })
                       .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Generates a formatted method signature with parameter names for display.
     * Example: "public static int sum(int param1, int param2)"
     */
    private String generateMethodSignature(Problem problem) {
        String methodName = problem.getMethodName() != null ? problem.getMethodName() : "solve";
        String returnType = problem.getReturnType() != null ? problem.getReturnType() : 
                           (problem.getOutputType() != null ? problem.getOutputType() : "String");
        String[] parameterTypes = problem.getParameterTypesArray();
        
        // Convert return type to Java type
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
    
    /**
     * Converts a type string to Java type format.
     */
    private String convertToJavaType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "String";
        }
        
        String normalizedType = type.trim().toLowerCase();
        
        // Check for array types first
        if (normalizedType.endsWith("[]")) {
            String baseType = normalizedType.substring(0, normalizedType.length() - 2).trim();
            String javaBaseType = convertBaseTypeToJava(baseType);
            return javaBaseType + "[]";
        }
        
        return convertBaseTypeToJava(normalizedType);
    }
    
    /**
     * Converts base type to Java type.
     */
    private String convertBaseTypeToJava(String type) {
        switch (type) {
            case "int": return "int";
            case "long": return "long";
            case "short": return "short";
            case "byte": return "byte";
            case "char": return "char";
            case "float": return "float";
            case "double": return "double";
            case "boolean": return "boolean";
            case "string": return "String";
            case "integer": return "Integer";
            case "character": return "Character";
            default: return "String";
        }
    }
}

