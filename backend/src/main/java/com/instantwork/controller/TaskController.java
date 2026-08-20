package com.instantwork.controller;

import com.instantwork.dto.TaskCreateRequest;
import com.instantwork.model.Task;
import com.instantwork.model.TaskStatus;
import com.instantwork.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double maxDistance,
            @RequestParam(required = false) Double minReward,
            @RequestParam(required = false) String sortByReward,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng
    ) {
        List<Task> tasks = taskService.searchAndFilterTasks(category, maxDistance, minReward, sortByReward, duration, status, keyword, userLat, userLng);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskCreateRequest request) {
        try {
            Task created = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptTask(@PathVariable Long id, @RequestParam Long workerId) {
        try {
            Task task = taskService.acceptTask(id, workerId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable Long id, @RequestParam Long workerId) {
        try {
            Task task = taskService.startTask(id, workerId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id, @RequestParam Long workerId) {
        try {
            Task task = taskService.completeTask(id, workerId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/release-payment")
    public ResponseEntity<?> releasePayment(@PathVariable Long id, @RequestParam Long posterId) {
        try {
            Task task = taskService.releasePayment(id, posterId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-posted")
    public ResponseEntity<List<Task>> getMyPostedTasks(
            @RequestParam Long userId,
            @RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByPosterAndStatus(userId, status));
        }
        return ResponseEntity.ok(taskService.getTasksPostedByUser(userId));
    }

    @GetMapping("/my-accepted")
    public ResponseEntity<List<Task>> getMyAcceptedTasks(
            @RequestParam Long workerId,
            @RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByWorkerAndStatus(workerId, status));
        }
        return ResponseEntity.ok(taskService.getTasksAcceptedByWorker(workerId));
    }
}
