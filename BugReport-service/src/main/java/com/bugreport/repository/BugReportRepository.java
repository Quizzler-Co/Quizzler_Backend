package com.bugreport.repository;

import com.bugreport.entities.BugReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugReportRepository extends MongoRepository<BugReport, String> {
    List<BugReport> findByReporterEmail(String email);
}

