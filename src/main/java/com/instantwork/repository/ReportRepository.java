package com.instantwork.repository;

import com.instantwork.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatusOrderByCreatedAtDesc(String status);
    List<Report> findAllByOrderByCreatedAtDesc();
    List<Report> findByReportedUserId(Long reportedUserId);
    List<Report> findByReportedTaskId(Long reportedTaskId);
}
