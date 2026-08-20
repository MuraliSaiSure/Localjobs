package com.instantwork.controller;

import com.instantwork.dto.AccountStatusUpdateRequest;
import com.instantwork.dto.VerificationReviewRequest;
import com.instantwork.model.*;
import com.instantwork.repository.TaskRepository;
import com.instantwork.repository.UserRepository;
import com.instantwork.service.SecurityService;
import com.instantwork.service.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final VerificationService verificationService;
    private final SecurityService securityService;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository,
                           VerificationService verificationService, SecurityService securityService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.verificationService = verificationService;
        this.securityService = securityService;
    }

    /**
     * RBAC: Validate Admin Role Header or Token
     */
    private boolean isAuthorizedAdmin(String roleHeader) {
        // Allows ADMIN role header or valid admin session
        return roleHeader != null && ("ADMIN".equalsIgnoreCase(roleHeader) || roleHeader.startsWith("ADMIN_SESSION"));
    }

    // 1. Platform Statistics Overview
    @GetMapping("/stats")
    public ResponseEntity<?> getPlatformStats(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader) {
        if (!isAuthorizedAdmin(roleHeader)) {
            // Return 403 Forbidden for unauthorized access
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }

        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userRepository.count();
        long totalTasks = taskRepository.count();
        long openTasks = taskRepository.countByStatus(TaskStatus.OPEN);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED) + taskRepository.countByStatus(TaskStatus.PAYMENT_RELEASED);

        List<Task> allTasks = taskRepository.findAll();
        double totalTaskValue = allTasks.stream()
                .mapToDouble(t -> t.getReward() != null ? t.getReward() : 0.0)
                .sum();

        stats.put("totalUsers", totalUsers);
        stats.put("totalTasks", totalTasks);
        stats.put("openTasks", openTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("totalTaskValue", totalTaskValue);
        stats.put("pendingVerifications", verificationService.getPendingVerifications().size());
        stats.put("pendingReports", securityService.getPendingReports().size());

        return ResponseEntity.ok(stats);
    }

    // 2. Pending Verification Requests
    @GetMapping("/verifications/pending")
    public ResponseEntity<?> getPendingVerifications(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    // 3. Review Verification Request (Approve / Reject / Re-verify)
    @PostMapping("/verifications/review")
    public ResponseEntity<?> reviewVerification(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader,
                                                @RequestBody VerificationReviewRequest request) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        try {
            User user = verificationService.reviewVerification(request);
            return ResponseEntity.ok(Map.of(
                    "userId", user.getId(),
                    "verificationStatus", user.getVerificationStatus().name(),
                    "verified", user.getVerified(),
                    "message", "Verification review submitted successfully."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. Incident Reports Management
    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        return ResponseEntity.ok(securityService.getAllReports());
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader,
                                           @PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        String adminUsername = body.getOrDefault("adminUsername", "admin");
        String decision = body.getOrDefault("decision", "RESOLVED");
        String notes = body.getOrDefault("notes", "Resolved by admin.");

        try {
            Report report = securityService.resolveReport(id, adminUsername, decision, notes);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Account Status Management (Suspend / Active / Under Review)
    @PutMapping("/users/{id}/account-status")
    public ResponseEntity<?> updateAccountStatus(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader,
                                                 @PathVariable Long id,
                                                 @RequestBody AccountStatusUpdateRequest request) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        request.setUserId(id);
        try {
            User user = securityService.updateAccountStatus(request);
            return ResponseEntity.ok(Map.of(
                    "userId", user.getId(),
                    "accountStatus", user.getAccountStatus().name(),
                    "message", "User status updated to " + user.getAccountStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Audit Trail Logs
    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        return ResponseEntity.ok(securityService.getAllAuditLogs());
    }

    // 7. Delete / Moderate Task
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@RequestHeader(value = "X-Admin-Role", required = false) String roleHeader,
                                        @PathVariable Long id) {
        if (!isAuthorizedAdmin(roleHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Admin privileges required."));
        }
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(id);
        securityService.recordAuditLog("admin", "TASK_REMOVED", "TASK", id, "Task #" + id, "Moderated and removed spam task");
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully by Admin"));
    }
}
