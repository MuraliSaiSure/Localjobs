package com.instantwork.controller;

import com.instantwork.model.Task;
import com.instantwork.model.TaskStatus;
import com.instantwork.model.User;
import com.instantwork.repository.ReviewRepository;
import com.instantwork.repository.TaskRepository;
import com.instantwork.repository.TransactionRepository;
import com.instantwork.repository.UserRepository;
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
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;

    public AdminController(UserRepository userRepository,
                           TaskRepository taskRepository,
                           TransactionRepository transactionRepository,
                           ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.transactionRepository = transactionRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalTasks = taskRepository.count();
        long totalTransactions = transactionRepository.count();
        long totalReviews = reviewRepository.count();

        List<Task> allTasks = taskRepository.findAll();
        long openTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.OPEN).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS || t.getStatus() == TaskStatus.ACCEPTED).count();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED || t.getStatus() == TaskStatus.PAYMENT_RELEASED).count();

        double totalTaskValue = allTasks.stream()
                .mapToDouble(t -> t.getReward() != null ? t.getReward() : 0.0)
                .sum();

        stats.put("totalUsers", totalUsers);
        stats.put("totalTasks", totalTasks);
        stats.put("openTasks", openTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("totalTaskValue", totalTaskValue);
        stats.put("totalTransactions", totalTransactions);
        stats.put("totalReviews", totalReviews);

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully by Admin"));
    }

    @PutMapping("/users/{id}/toggle-verify")
    public ResponseEntity<?> toggleUserVerification(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setVerified(user.getVerified() == null || !user.getVerified());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
