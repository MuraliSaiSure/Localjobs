package com.instantwork.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long posterId;

    private String posterName;
    private Double posterRating = 5.0;
    private Integer posterCompletedTasks = 0;

    private Long workerId;
    private String workerName;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_skills", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "skill")
    private List<String> requiredSkills = new ArrayList<>();

    @Column(nullable = false)
    private Double reward;

    @Column(nullable = false)
    private String duration;

    private String date;
    private String startTime;
    private String endTime;

    @Column(nullable = false)
    private String location;

    private Double latitude;
    private Double longitude;

    @Transient
    private Double distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public Task() {}

    public Task(Long posterId, String posterName, Double posterRating, Integer posterCompletedTasks,
                String title, String description, String category, List<String> requiredSkills,
                Double reward, String duration, String date, String startTime, String endTime,
                String location, Double latitude, Double longitude) {
        this.posterId = posterId;
        this.posterName = posterName;
        this.posterRating = posterRating;
        this.posterCompletedTasks = posterCompletedTasks;
        this.title = title;
        this.description = description;
        this.category = category;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.reward = reward;
        this.duration = duration;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = TaskStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPosterId() { return posterId; }
    public void setPosterId(Long posterId) { this.posterId = posterId; }

    public String getPosterName() { return posterName; }
    public void setPosterName(String posterName) { this.posterName = posterName; }

    public Double getPosterRating() { return posterRating; }
    public void setPosterRating(Double posterRating) { this.posterRating = posterRating; }

    public Integer getPosterCompletedTasks() { return posterCompletedTasks; }
    public void setPosterCompletedTasks(Integer posterCompletedTasks) { this.posterCompletedTasks = posterCompletedTasks; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public Double getReward() { return reward; }
    public void setReward(Double reward) { this.reward = reward; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
