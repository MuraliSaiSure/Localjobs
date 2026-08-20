package com.instantwork.dto;

public class ReportRequest {
    private Long reporterUserId;
    private Long reportedUserId;
    private Long reportedTaskId;
    private String reportType; // USER_REPORT, TASK_REPORT
    private String reason;     // Fraud, Fake identity, Harassment, Suspicious behavior, Payment problems, Other
    private String description;

    public ReportRequest() {}

    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public Long getReportedTaskId() { return reportedTaskId; }
    public void setReportedTaskId(Long reportedTaskId) { this.reportedTaskId = reportedTaskId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
