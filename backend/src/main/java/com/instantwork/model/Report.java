package com.instantwork.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterUserId;
    private String reporterName;

    private Long reportedUserId;
    private String reportedUserName;

    private Long reportedTaskId;
    private String reportedTaskTitle;

    private String reportType; // USER_REPORT or TASK_REPORT
    private String reason;     // Fraud, Fake identity, Harassment, Suspicious behavior, Payment problems, Other
    
    @Column(length = 1000)
    private String description;

    private String status; // PENDING, RESOLVED, DISMISSED

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String resolutionNotes;

    public Report() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Report(Long reporterUserId, String reporterName, Long reportedUserId, String reportedUserName,
                  Long reportedTaskId, String reportedTaskTitle, String reportType, String reason, String description) {
        this.reporterUserId = reporterUserId;
        this.reporterName = reporterName;
        this.reportedUserId = reportedUserId;
        this.reportedUserName = reportedUserName;
        this.reportedTaskId = reportedTaskId;
        this.reportedTaskTitle = reportedTaskTitle;
        this.reportType = reportType;
        this.reason = reason;
        this.description = description;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReportedUserName() { return reportedUserName; }
    public void setReportedUserName(String reportedUserName) { this.reportedUserName = reportedUserName; }

    public Long getReportedTaskId() { return reportedTaskId; }
    public void setReportedTaskId(Long reportedTaskId) { this.reportedTaskId = reportedTaskId; }

    public String getReportedTaskTitle() { return reportedTaskTitle; }
    public void setReportedTaskTitle(String reportedTaskTitle) { this.reportedTaskTitle = reportedTaskTitle; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
