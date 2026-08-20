package com.instantwork.service;

import com.instantwork.dto.VerificationReviewRequest;
import com.instantwork.dto.VerificationSubmissionRequest;
import com.instantwork.model.AuditLog;
import com.instantwork.model.User;
import com.instantwork.model.VerificationStatus;
import com.instantwork.repository.AuditLogRepository;
import com.instantwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;

    public VerificationService(UserRepository userRepository, NotificationService notificationService, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Privacy-Preserving Document Masking
     * Aadhaar (12 digits): "XXXX-XXXX-1234"
     * PAN (10 chars): "XXXXX1234X"
     */
    public String maskDocumentNumber(String docType, String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            return "XXXX-XXXX-XXXX";
        }
        String clean = rawNumber.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        if ("AADHAAR".equalsIgnoreCase(docType)) {
            if (clean.length() >= 4) {
                String last4 = clean.substring(clean.length() - 4);
                return "XXXX-XXXX-" + last4;
            }
            return "XXXX-XXXX-XXXX";
        } else if ("PAN".equalsIgnoreCase(docType) || "PAN_CARD".equalsIgnoreCase(docType)) {
            if (clean.length() >= 5) {
                String middleAndEnd = clean.substring(clean.length() - 5);
                return "XXXXX" + middleAndEnd;
            }
            return "XXXXX1234X";
        }
        return "XXXX-XXXX-XXXX";
    }

    @Transactional
    public User submitVerification(VerificationSubmissionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        if (request.getDocNumber() == null || request.getDocNumber().trim().length() < 4) {
            throw new IllegalArgumentException("Please enter a valid document number.");
        }
        if (request.getNameOnDoc() == null || request.getNameOnDoc().trim().isEmpty()) {
            throw new IllegalArgumentException("Name as per government ID is required.");
        }

        String docType = (request.getDocType() != null && !request.getDocType().isEmpty()) ? request.getDocType().toUpperCase() : "AADHAAR";
        String maskedNumber = maskDocumentNumber(docType, request.getDocNumber());

        user.setVerificationDocType(docType);
        user.setMaskedDocNumber(maskedNumber);
        user.setNameOnDoc(request.getNameOnDoc().trim());
        user.setVerificationStatus(VerificationStatus.VERIFICATION_PENDING);
        user.setVerificationSubmittedAt(LocalDateTime.now());
        user.setVerificationRemarks(null);

        User updatedUser = userRepository.save(user);

        // Notify user
        notificationService.createNotification(
                user.getId(),
                "Identity Verification Submitted 📋",
                "Your " + docType + " verification is currently under review by platform administrators.",
                "VERIFICATION_PENDING",
                null
        );

        return updatedUser;
    }

    public List<User> getPendingVerifications() {
        return userRepository.findByVerificationStatus(VerificationStatus.VERIFICATION_PENDING);
    }

    @Transactional
    public User reviewVerification(VerificationReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        String decision = request.getDecision() != null ? request.getDecision().toUpperCase() : "APPROVED";
        String actionName;

        if ("APPROVED".equals(decision) || "APPROVE".equals(decision)) {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
            user.setVerified(true);
            user.setVerificationRemarks("Verified by Admin on " + LocalDateTime.now().toLocalDate());
            actionName = "VERIFICATION_APPROVED";

            notificationService.createNotification(
                    user.getId(),
                    "Identity Verified Successfully! ✓",
                    "Congratulations! Your account is now verified. You have full access to post and accept micro-tasks.",
                    "VERIFICATION_APPROVED",
                    null
            );
        } else if ("REJECTED".equals(decision) || "REJECT".equals(decision)) {
            user.setVerificationStatus(VerificationStatus.REJECTED);
            user.setVerified(false);
            user.setVerificationRemarks(request.getRemarks() != null ? request.getRemarks() : "Document details could not be verified.");
            actionName = "VERIFICATION_REJECTED";

            notificationService.createNotification(
                    user.getId(),
                    "Identity Verification Rejected ⚠️",
                    "Reason: " + user.getVerificationRemarks() + ". Please resubmit with valid details.",
                    "VERIFICATION_REJECTED",
                    null
            );
        } else {
            user.setVerificationStatus(VerificationStatus.REQUIRES_REVERIFICATION);
            user.setVerified(false);
            user.setVerificationRemarks(request.getRemarks() != null ? request.getRemarks() : "Please re-upload your document.");
            actionName = "VERIFICATION_RE_REQUESTED";

            notificationService.createNotification(
                    user.getId(),
                    "Re-Verification Required 🔄",
                    "Note from admin: " + user.getVerificationRemarks(),
                    "REQUIRES_REVERIFICATION",
                    null
            );
        }

        User updatedUser = userRepository.save(user);

        // Audit Log
        auditLogRepository.save(new AuditLog(
                request.getAdminUsername() != null ? request.getAdminUsername() : "admin",
                actionName,
                "USER_VERIFICATION",
                user.getId(),
                user.getName(),
                request.getRemarks() != null ? request.getRemarks() : "Decision: " + decision
        ));

        return updatedUser;
    }
}
