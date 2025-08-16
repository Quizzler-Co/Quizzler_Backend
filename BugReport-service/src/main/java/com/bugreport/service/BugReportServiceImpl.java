package com.bugreport.service;

import com.bugreport.DTO.BugReportRequest;
import com.bugreport.entities.BugReport;
import com.bugreport.repository.BugReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BugReportServiceImpl implements BugReportService {

    private final BugReportRepository bugReportRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Override
    public void submitBug(BugReportRequest request, String userEmail) {
        BugReport bug = modelMapper.map(request, BugReport.class);
        bug.setReporterEmail(userEmail);
        bug.setStatus("OPEN");
        bug.setCreatedAt(LocalDateTime.now());

        bugReportRepository.save(bug);

        String subject = "Bug Report for Quiz ID: " + bug.getQuizId();
        String body = """
                A user has reported a bug.

                Quiz ID: %s
                Question ID: %s
                Title: %s
                Severity: %s
                Description: %s

                Reporter: %s
                """.formatted(
                bug.getQuizId(),
                bug.getQuestionId() != null ? bug.getQuestionId() : "N/A",
                bug.getTitle(),
                bug.getSeverity(),
                bug.getDescription(),
                bug.getReporterEmail()
        );

        // Send admin and user emails asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail("admin@quizzler.com", subject, body);
            } catch (Exception e) {
                log.error("Failed to send email to admin", e);
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail(
                        userEmail,
                        "Thanks for reporting a bug",
                        "Your report for Quiz ID " + bug.getQuizId() + " has been received. We’ll review it shortly."
                );
            } catch (Exception e) {
                log.error("Failed to send acknowledgment email to user", e);
            }
        });

        log.info("Bug saved & email dispatch initiated for Quiz ID: {}", bug.getQuizId());
    }

    @Override
    public List<BugReport> getBugsByEmail(String email) {
        return bugReportRepository.findByReporterEmail(email);
    }

    @Override
    public List<BugReport> getAllBugs() {
        return bugReportRepository.findAll();
    }

    @Override
    public void updateBugStatus(String bugId, String status) {
        BugReport bug = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found with ID: " + bugId));
        bug.setStatus(status.toUpperCase());
        bugReportRepository.save(bug);
    }

    @Override
    public void deleteBug(String bugId) {
        if (!bugReportRepository.existsById(bugId)) {
            throw new RuntimeException("Bug not found with ID: " + bugId);
        }
        bugReportRepository.deleteById(bugId);
    }
}
