package com.instantwork.controller;

import com.instantwork.dto.RatingRequest;
import com.instantwork.model.Review;
import com.instantwork.model.Task;
import com.instantwork.service.ReviewService;
import com.instantwork.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final ReviewService reviewService;
    private final TaskService taskService;

    public RatingController(ReviewService reviewService, TaskService taskService) {
        this.reviewService = reviewService;
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody RatingRequest request) {
        try {
            String taskTitle = "Task #" + request.getTaskId();
            Task task = taskService.getTaskById(request.getTaskId()).orElse(null);
            if (task != null) {
                taskTitle = task.getTitle();
            }

            Review saved = reviewService.submitReview(request, taskTitle);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }
}
