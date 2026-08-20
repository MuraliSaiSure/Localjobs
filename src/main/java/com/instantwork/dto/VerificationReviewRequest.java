package com.instantwork.dto;

public class VerificationReviewRequest {
    private String adminUsername;
    private Long userId;
    private String decision; // APPROVED, REJECTED, RE_VERIFY
    private String remarks;

    public VerificationReviewRequest() {}

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
