package com.instantwork.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long taskId;
    private String taskTitle;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String type; // CREDIT, DEBIT, ESCROW_RELEASE

    private String description;

    private String status = "SUCCESS";

    private LocalDateTime timestamp = LocalDateTime.now();

    public Transaction() {}

    public Transaction(Long userId, Long taskId, String taskTitle, Double amount, String type, String description) {
        this.userId = userId;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.status = "SUCCESS";
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
