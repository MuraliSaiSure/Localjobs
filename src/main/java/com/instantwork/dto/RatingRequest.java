package com.instantwork.dto;

public class RatingRequest {
    private Long taskId;
    private Long fromUserId;
    private Long toUserId;
    private Double rating;
    private String reviewText;
    private String role; // WORKER_RATING_POSTER or POSTER_RATING_WORKER

    public RatingRequest() {}

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
