package com.instantwork.controller;

import com.instantwork.dto.BlockUserRequest;
import com.instantwork.dto.ReportRequest;
import com.instantwork.model.Report;
import com.instantwork.model.User;
import com.instantwork.service.SecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityService securityService;

    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping("/report")
    public ResponseEntity<?> fileReport(@RequestBody ReportRequest request) {
        try {
            Report report = securityService.fileReport(request);
            return ResponseEntity.ok(Map.of(
                    "reportId", report.getId(),
                    "status", report.getStatus(),
                    "message", "Report filed successfully. Our moderation team has been notified."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to file report."));
        }
    }

    @PostMapping("/block")
    public ResponseEntity<?> blockUser(@RequestBody BlockUserRequest request) {
        try {
            User user = securityService.blockUser(request.getUserId(), request.getTargetUserId());
            return ResponseEntity.ok(Map.of(
                    "message", "User blocked successfully.",
                    "blockedUserIds", user.getBlockedUserIds()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/unblock")
    public ResponseEntity<?> unblockUser(@RequestBody BlockUserRequest request) {
        try {
            User user = securityService.unblockUser(request.getUserId(), request.getTargetUserId());
            return ResponseEntity.ok(Map.of(
                    "message", "User unblocked successfully.",
                    "blockedUserIds", user.getBlockedUserIds()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
