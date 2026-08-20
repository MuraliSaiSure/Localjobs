package com.instantwork.dto;

public class AccountStatusUpdateRequest {
    private String adminUsername;
    private Long userId;
    private String newStatus; // ACTIVE, SUSPENDED, BLOCKED, UNDER_REVIEW
    private String reason;

    public AccountStatusUpdateRequest() {}

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
