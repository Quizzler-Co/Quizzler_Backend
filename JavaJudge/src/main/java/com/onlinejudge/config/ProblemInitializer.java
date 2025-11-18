package com.onlinejudge.config;

import com.onlinejudge.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProblemInitializer implements CommandLineRunner {

    @Autowired
    private ProblemService problemService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize problems from filesystem on startup
        problemService.initializeProblemsFromFilesystem();
    }
}

