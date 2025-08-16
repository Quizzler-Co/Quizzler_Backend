package com.bugreport.service;

import com.bugreport.DTO.BugReportRequest;
import com.bugreport.entities.BugReport;


import java.util.List;

public interface BugReportService {

    /**
     * Submits a new bug report.
     *
     * @param request   The bug report data.
     * @param userEmail The email of the user submitting the report.
     */
    void submitBug(BugReportRequest request, String userEmail);

    /**
     * Retrieves all bug reports submitted by a specific user.
     *
     * @param email The user's email address.
     * @return List of bug reports.
     */
    List<BugReport> getBugsByEmail(String email);

    /**
     * Retrieves all bug reports (admin use).
     *
     * @return List of all bug reports in the system.
     */
    List<BugReport> getAllBugs();

    /**
     * Updates the status of a bug report.
     *
     * @param bugId  The ID of the bug to update.
     * @param status The new status to set (e.g., OPEN, IN_PROGRESS, RESOLVED).
     */
    void updateBugStatus(String bugId, String status);

    /**
     * Deletes a bug report by its ID.
     *
     * @param bugId The ID of the bug to delete.
     */
    void deleteBug(String bugId);
}
