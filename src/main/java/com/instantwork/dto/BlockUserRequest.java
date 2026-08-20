package com.instantwork.dto;

public class BlockUserRequest {
    private Long userId;
    private Long targetUserId;

    public BlockUserRequest() {}

    public BlockUserRequest(Long userId, Long targetUserId) {
        this.userId = userId;
        this.targetUserId = targetUserId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
}
