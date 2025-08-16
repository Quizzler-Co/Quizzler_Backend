package com.bugreport.controller;

import com.bugreport.DTO.BugReportRequest;
import com.bugreport.entities.BugReport;
import com.bugreport.service.BugReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bugs")
@RequiredArgsConstructor
@Slf4j
public class BugReportController {

    private final BugReportService bugReportService;

    //  1. Submit bug — open to all authenticated users
    @PostMapping("/submit")
    public ResponseEntity<String> submitBug(
            @RequestBody BugReportRequest request,
            @RequestHeader("x-email") String userEmail
    ) {
        log.info("Received bug report from {}", userEmail);
        bugReportService.submitBug(request, userEmail);
        log.info("Bug saved & email sent successfully for {}", request.getQuizId());
        return ResponseEntity.ok("Bug submitted successfully");
    }


    //  2. View own bugs — for regular users
    @GetMapping("/my-reports")
    public ResponseEntity<List<BugReport>> getMyReports(
            @RequestHeader("x-user-email") String userEmail
    ) {
        return ResponseEntity.ok(bugReportService.getBugsByEmail(userEmail));
    }

    //  3. View all bugs — admin only
    @GetMapping("/all")
    public ResponseEntity<?> getAllBugs(@RequestHeader("x-role") String role) {
        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        return ResponseEntity.ok(bugReportService.getAllBugs());
    }

    //  4. Update bug status — admin only
    @PutMapping("/{bugId}")
    public ResponseEntity<?> updateBugStatus(
            @PathVariable String bugId,
            @RequestParam String status,
            @RequestHeader("x-role") String role
    ) {
        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Admins only");
        }
        bugReportService.updateBugStatus(bugId, status);
        return ResponseEntity.ok("Bug status updated successfully");
    }

    //  5. Delete bug report — admin only
    @DeleteMapping("/{bugId}")
    public ResponseEntity<?> deleteBug(
            @PathVariable String bugId,
            @RequestHeader("x-role") String role
    ) {
        if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Admins only");
        }
        bugReportService.deleteBug(bugId);
        return ResponseEntity.ok("Bug deleted successfully");
    }
}
