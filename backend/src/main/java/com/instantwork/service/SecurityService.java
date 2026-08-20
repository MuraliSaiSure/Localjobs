package com.instantwork.service;

import com.instantwork.dto.AccountStatusUpdateRequest;
import com.instantwork.dto.ReportRequest;
import com.instantwork.model.*;
import com.instantwork.repository.AuditLogRepository;
import com.instantwork.repository.ReportRepository;
import com.instantwork.repository.TaskRepository;
import com.instantwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityService {

    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public SecurityService(ReportRepository reportRepository, AuditLogRepository auditLogRepository,
                           UserRepository userRepository, TaskRepository taskRepository,
                           NotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    // 1. File Incident Report (User or Task)
    @Transactional
    public Report fileReport(ReportRequest request) {
        User reporter = userRepository.findById(request.getReporterUserId())
                .orElseThrow(() -> new IllegalArgumentException("Reporter user not found."));

        String reportedUserName = null;
        if (request.getReportedUserId() != null) {
            userRepository.findById(request.getReportedUserId())
                    .ifPresent(u -> request.setReportedUserId(u.getId()));
            reportedUserName = userRepository.findById(request.getReportedUserId()).map(User::getName).orElse(null);
        }

        String reportedTaskTitle = null;
        if (request.getReportedTaskId() != null) {
            reportedTaskTitle = taskRepository.findById(request.getReportedTaskId()).map(Task::getTitle).orElse(null);
        }

        Report report = new Report(
                reporter.getId(),
                reporter.getName(),
                request.getReportedUserId(),
                reportedUserName,
                request.getReportedTaskId(),
                reportedTaskTitle,
                request.getReportType() != null ? request.getReportType() : "USER_REPORT",
                request.getReason() != null ? request.getReason() : "Suspicious behavior",
                request.getDescription()
        );

        Report savedReport = reportRepository.save(report);

        // Security Notification to reporter
        notificationService.createNotification(
                reporter.getId(),
                "Report Submitted 🛡️",
                "Your incident report has been submitted to platform moderators for investigation.",
                "REPORT_FILED",
                null
        );

        return savedReport;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Transactional
    public Report resolveReport(Long reportId, String adminUsername, String decision, String notes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        report.setStatus(decision != null ? decision.toUpperCase() : "RESOLVED");
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminUsername != null ? adminUsername : "admin");
        report.setResolutionNotes(notes);

        Report saved = reportRepository.save(report);

        // Audit Log
        auditLogRepository.save(new AuditLog(
                adminUsername != null ? adminUsername : "admin",
                "REPORT_RESOLVED",
                "REPORT",
                report.getId(),
                "Report #" + report.getId() + " (" + report.getReportType() + ")",
                notes
        ));

        return saved;
    }

    // 2. Block / Unblock User
    @Transactional
    public User blockUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.getBlockedUserIds().add(targetUserId);
        return userRepository.save(user);
    }

    @Transactional
    public User unblockUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.getBlockedUserIds().remove(targetUserId);
        return userRepository.save(user);
    }

    // 3. Admin Account Suspension & Reactivation
    @Transactional
    public User updateAccountStatus(AccountStatusUpdateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        AccountStatus newStatus = AccountStatus.valueOf(request.getNewStatus().toUpperCase());
        user.setAccountStatus(newStatus);
        User updated = userRepository.save(user);

        // Audit Log
        auditLogRepository.save(new AuditLog(
                request.getAdminUsername() != null ? request.getAdminUsername() : "admin",
                newStatus == AccountStatus.SUSPENDED ? "USER_SUSPENDED" : "USER_STATUS_UPDATED",
                "USER",
                user.getId(),
                user.getName(),
                request.getReason() != null ? request.getReason() : "Status updated to " + newStatus
        ));

        // Security Alert to user
        if (newStatus == AccountStatus.SUSPENDED) {
            notificationService.createNotification(
                    user.getId(),
                    "Account Suspended ⚠️",
                    "Your account has been suspended by administrators. Reason: " + request.getReason(),
                    "ACCOUNT_SUSPENDED",
                    null
            );
        } else if (newStatus == AccountStatus.ACTIVE) {
            notificationService.createNotification(
                    user.getId(),
                    "Account Reactivated 🟢",
                    "Your account status is now Active.",
                    "ACCOUNT_ACTIVE",
                    null
            );
        }

        return updated;
    }

    // 4. Audit Logs
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public AuditLog recordAuditLog(String adminUsername, String action, String targetType, Long targetId, String targetName, String reason) {
        return auditLogRepository.save(new AuditLog(adminUsername, action, targetType, targetId, targetName, reason));
    }
}
