package com.onlinejudge.service;

import com.onlinejudge.entity.Problem;
import com.onlinejudge.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProblemService {

    @Autowired
    private ProblemRepository problemRepository;

    private static final String PROBLEMS_BASE_DIRECTORY = "judge/problems";

    /**
     * Initialize problems from filesystem - scans the problems directory
     * and creates/updates problem records in the database
     */
    public void initializeProblemsFromFilesystem() {
        try {
            ClassPathResource problemsResource = new ClassPathResource(PROBLEMS_BASE_DIRECTORY);
            if (problemsResource.exists()) {
                File problemsDir = problemsResource.getFile();
                File[] problemDirs = problemsDir.listFiles(File::isDirectory);
                
                if (problemDirs != null) {
                    for (File problemDir : problemDirs) {
                        String problemId = problemDir.getName();
                        
                        // Check if problem already exists
                        Problem problem;
                        if (problemRepository.existsById(problemId)) {
                            problem = problemRepository.findById(problemId).orElse(null);
                            if (problem == null) continue;
                        } else {
                            // Create new problem with default values
                            problem = new Problem();
                            problem.setId(problemId);
                            problem.setTitle("Problem " + problemId);
                            problem.setDescription("Solve this problem");
                            problem.setDifficulty("MEDIUM");
                            problem.setActive(true);
                            problem.setInputType("String");
                            problem.setOutputType("String");
                            // Set default method signature
                            problem.setMethodName("solve");
                            problem.setParameterTypes("String");
                            problem.setReturnType("String");
                        }
                        
                        // Try to read description from statement.txt if it exists
                        Path statementPath = Paths.get(problemDir.getPath(), "statement.txt");
                        if (Files.exists(statementPath)) {
                            try {
                                String description = Files.readString(statementPath);
                                // Extract title from first line if it exists
                                String[] lines = description.split("\n", 2);
                                if (lines.length > 0 && !lines[0].trim().isEmpty()) {
                                    problem.setTitle(lines[0].trim());
                                    // Set description as the full statement (or remaining lines)
                                    problem.setDescription(description);
                                } else {
                                    problem.setDescription(description);
                                }
                            } catch (IOException e) {
                                // Ignore if can't read statement
                            }
                        }
                        
                        // Always update method signature types (even for existing problems)
                        // Set types for example1 (int input and int output)
                        if ("example1".equals(problemId)) {
                            problem.setInputType("int");
                            problem.setOutputType("int");
                            problem.setMethodName("solve");
                            problem.setParameterTypes("int");
                            problem.setReturnType("int");
                        }
                        
                        // Set types for example2 (int input and int output)
                        if ("example2".equals(problemId)) {
                            problem.setInputType("int");
                            problem.setOutputType("int");
                            problem.setMethodName("solve");
                            problem.setParameterTypes("int");
                            problem.setReturnType("int");
                        }
                        
                        // Set types for reverse-string (String input and String output)
                        if ("reverse-string".equals(problemId)) {
                            problem.setInputType("String");
                            problem.setOutputType("String");
                            problem.setMethodName("reverse");
                            problem.setParameterTypes("String");
                            problem.setReturnType("String");
                        }
                        
                        // Set types for reverse-array (int[] input and int[] output)
                        if ("reverse-array".equals(problemId)) {
                            problem.setInputType("int[]");
                            problem.setOutputType("int[]");
                            problem.setMethodName("reverseArray");
                            problem.setParameterTypes("int[]");
                            problem.setReturnType("int[]");
                        }
                        
                        // Set types for sum-two-numbers (two int parameters, int output)
                        if ("sum-two-numbers".equals(problemId)) {
                            problem.setInputType("int");
                            problem.setOutputType("int");
                            problem.setMethodName("sum");
                            problem.setParameterTypes("int,int");
                            problem.setReturnType("int");
                            problem.setDifficulty("EASY");
                        }
                        
                        // Set types for find-maximum (int[] input, int output)
                        if ("find-maximum".equals(problemId)) {
                            problem.setInputType("int[]");
                            problem.setOutputType("int");
                            problem.setMethodName("findMax");
                            problem.setParameterTypes("int[]");
                            problem.setReturnType("int");
                            problem.setDifficulty("EASY");
                        }
                        
                        // Set types for count-vowels (String input, int output)
                        if ("count-vowels".equals(problemId)) {
                            problem.setInputType("String");
                            problem.setOutputType("int");
                            problem.setMethodName("countVowels");
                            problem.setParameterTypes("String");
                            problem.setReturnType("int");
                            problem.setDifficulty("MEDIUM");
                        }
                        
                        // Set types for factorial (int input, long output)
                        if ("factorial".equals(problemId)) {
                            problem.setInputType("int");
                            problem.setOutputType("long");
                            problem.setMethodName("factorial");
                            problem.setParameterTypes("int");
                            problem.setReturnType("long");
                            problem.setDifficulty("MEDIUM");
                        }
                        
                        // Set types for find-average (int[] input, double output)
                        if ("find-average".equals(problemId)) {
                            problem.setInputType("int[]");
                            problem.setOutputType("double");
                            problem.setMethodName("findAverage");
                            problem.setParameterTypes("int[]");
                            problem.setReturnType("double");
                            problem.setDifficulty("EASY");
                        }
                        
                        // Set types for power (two int parameters, long output)
                        if ("power".equals(problemId)) {
                            problem.setInputType("int");
                            problem.setOutputType("long");
                            problem.setMethodName("power");
                            problem.setParameterTypes("int,int");
                            problem.setReturnType("long");
                            problem.setDifficulty("MEDIUM");
                        }
                        
                        problemRepository.save(problem);
                    }
                }
            }
        } catch (IOException e) {
            // If running from JAR, filesystem access might not work
            // In that case, problems should be seeded manually or via API
        }
    }
}

