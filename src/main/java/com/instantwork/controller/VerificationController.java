package com.instantwork.controller;

import com.instantwork.dto.VerificationSubmissionRequest;
import com.instantwork.model.User;
import com.instantwork.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitVerification(@RequestBody VerificationSubmissionRequest request) {
        try {
            User user = verificationService.submitVerification(request);
            return ResponseEntity.ok(Map.of(
                    "userId", user.getId(),
                    "verificationStatus", user.getVerificationStatus().name(),
                    "maskedDocNumber", user.getMaskedDocNumber() != null ? user.getMaskedDocNumber() : "",
                    "docType", user.getVerificationDocType() != null ? user.getVerificationDocType() : "",
                    "message", "Verification submitted successfully. Admin review is pending."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to submit verification"));
        }
    }
}
