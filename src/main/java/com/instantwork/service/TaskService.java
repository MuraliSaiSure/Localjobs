package com.instantwork.service;

import com.instantwork.dto.TaskCreateRequest;
import com.instantwork.model.Task;
import com.instantwork.model.TaskStatus;
import com.instantwork.model.User;
import com.instantwork.model.VerificationStatus;
import com.instantwork.repository.TaskRepository;
import com.instantwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       WalletService walletService,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.notificationService = notificationService;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> searchAndFilterTasks(String category,
                                          Double maxDistanceKm,
                                          Double minReward,
                                          String sortByReward,
                                          String duration,
                                          TaskStatus status,
                                          String keyword,
                                          Double userLat,
                                          Double userLng) {
        // Default to OPEN tasks for discovery if status is not explicitly set
        TaskStatus queryStatus = status != null ? status : TaskStatus.OPEN;
        String queryCategory = (category != null && !category.equalsIgnoreCase("All") && !category.trim().isEmpty()) ? category : null;
        String queryKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        List<Task> tasks = taskRepository.searchTasks(queryStatus, queryCategory, queryKeyword);

        // Fallback default coordinates (e.g., Ongole center) if not supplied
        double currentLat = userLat != null ? userLat : 15.5057;
        double currentLng = userLng != null ? userLng : 80.0499;

        // Calculate distance for each task
        for (Task t : tasks) {
            double tLat = t.getLatitude() != null ? t.getLatitude() : currentLat;
            double tLng = t.getLongitude() != null ? t.getLongitude() : currentLng;
            double dist = calculateDistance(currentLat, currentLng, tLat, tLng);
            t.setDistanceKm(Math.round(dist * 10.0) / 10.0);
        }

        // Apply filters
        List<Task> filtered = tasks.stream()
                .filter(t -> {
                    if (maxDistanceKm != null && maxDistanceKm > 0) {
                        return t.getDistanceKm() != null && t.getDistanceKm() <= maxDistanceKm;
                    }
                    return true;
                })
                .filter(t -> {
                    if (minReward != null && minReward > 0) {
                        return t.getReward() != null && t.getReward() >= minReward;
                    }
                    return true;
                })
                .filter(t -> {
                    if (duration != null && !duration.equalsIgnoreCase("All") && !duration.trim().isEmpty()) {
                        return t.getDuration() != null && t.getDuration().toLowerCase().contains(duration.toLowerCase());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Apply sorting
        if ("highest".equalsIgnoreCase(sortByReward)) {
            filtered.sort(Comparator.comparing(Task::getReward, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("lowest".equalsIgnoreCase(sortByReward)) {
            filtered.sort(Comparator.comparing(Task::getReward, Comparator.nullsLast(Comparator.naturalOrder())));
        } else {
            // Default sort: nearest first
            filtered.sort(Comparator.comparing(Task::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        return filtered;
    }

    @Transactional
    public Task createTask(TaskCreateRequest request) {
        User poster = userRepository.findById(request.getPosterId())
                .orElseThrow(() -> new RuntimeException("Poster user not found: " + request.getPosterId()));

        // Verification Requirement Guard
        if (!Boolean.TRUE.equals(poster.getVerified()) && poster.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Please complete identity verification to use this feature.");
        }

        Task task = new Task(
                poster.getId(),
                poster.getName(),
                poster.getRating(),
                poster.getCompletedTasks(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getRequiredSkills(),
                request.getReward(),
                request.getDuration(),
                request.getDate() != null ? request.getDate() : "Today",
                request.getStartTime() != null ? request.getStartTime() : "Flexible",
                request.getEndTime() != null ? request.getEndTime() : "Flexible",
                request.getLocation() != null ? request.getLocation() : poster.getLocation(),
                request.getLatitude() != null ? request.getLatitude() : poster.getLatitude(),
                request.getLongitude() != null ? request.getLongitude() : poster.getLongitude()
        );

        Task saved = taskRepository.save(task);

        notificationService.createNotification(
                poster.getId(),
                "Task Published Successfully 📢",
                "Your task '" + task.getTitle() + "' (₹" + task.getReward() + ") is now live for nearby workers.",
                "TASK_PUBLISHED",
                saved.getId()
        );

        return saved;
    }

    @Transactional
    public Task acceptTask(Long taskId, Long workerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new IllegalStateException("Task is not open for acceptance. Current status: " + task.getStatus());
        }

        if (task.getPosterId().equals(workerId)) {
            throw new IllegalArgumentException("You cannot accept your own posted task.");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker user not found: " + workerId));

        // Verification Requirement Guard
        if (!Boolean.TRUE.equals(worker.getVerified()) && worker.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Please complete identity verification to use this feature.");
        }

        User poster = userRepository.findById(task.getPosterId()).orElse(null);
        if (poster != null && (poster.getBlockedUserIds().contains(workerId) || worker.getBlockedUserIds().contains(poster.getId()))) {
            throw new IllegalStateException("Interaction restricted due to user blocking settings.");
        }

        task.setWorkerId(worker.getId());
        task.setWorkerName(worker.getName());
        task.setStatus(TaskStatus.ACCEPTED);

        Task saved = taskRepository.save(task);

        // Notify Poster
        notificationService.createNotification(
                task.getPosterId(),
                "Task Accepted! 🤝",
                worker.getName() + " accepted your task: " + task.getTitle(),
                "TASK_ACCEPTED",
                taskId
        );

        // Notify Worker
        notificationService.createNotification(
                worker.getId(),
                "You Accepted a Task! 🚀",
                "You are assigned to: " + task.getTitle() + ". Remember to start when ready.",
                "TASK_ACCEPTED",
                taskId
        );

        return saved;
    }

    @Transactional
    public Task startTask(Long taskId, Long workerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.ACCEPTED) {
            throw new IllegalStateException("Task cannot be started. Status must be ACCEPTED, but was: " + task.getStatus());
        }

        if (!task.getWorkerId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can start this task.");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        Task saved = taskRepository.save(task);

        notificationService.createNotification(
                task.getPosterId(),
                "Task In Progress ⏱️",
                task.getWorkerName() + " has started working on: " + task.getTitle(),
                "TASK_STARTED",
                taskId
        );

        return saved;
    }

    @Transactional
    public Task completeTask(Long taskId, Long workerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task cannot be completed. Status must be IN_PROGRESS, but was: " + task.getStatus());
        }

        if (!task.getWorkerId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can mark this task completed.");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        notificationService.createNotification(
                task.getPosterId(),
                "Task Completed — Review Needed! ✅",
                task.getWorkerName() + " marked '" + task.getTitle() + "' as completed. Please confirm to release reward ₹" + task.getReward(),
                "TASK_COMPLETED",
                taskId
        );

        return saved;
    }

    @Transactional
    public Task releasePayment(Long taskId, Long posterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Payment can only be released for COMPLETED tasks. Status: " + task.getStatus());
        }

        if (!task.getPosterId().equals(posterId)) {
            throw new IllegalArgumentException("Only the task poster can confirm completion and release payment.");
        }

        task.setStatus(TaskStatus.PAYMENT_RELEASED);
        Task saved = taskRepository.save(task);

        // Process wallet transaction for worker
        walletService.processRewardPayout(task.getWorkerId(), task.getId(), task.getTitle(), task.getReward());

        // Increment completed tasks count for both users
        User poster = userRepository.findById(posterId).orElse(null);
        if (poster != null) {
            poster.setCompletedTasks((poster.getCompletedTasks() != null ? poster.getCompletedTasks() : 0) + 1);
            userRepository.save(poster);
        }

        User worker = userRepository.findById(task.getWorkerId()).orElse(null);
        if (worker != null) {
            worker.setCompletedTasks((worker.getCompletedTasks() != null ? worker.getCompletedTasks() : 0) + 1);
            userRepository.save(worker);
        }

        // Notification for Worker
        notificationService.createNotification(
                task.getWorkerId(),
                "Reward Released! 💰",
                "₹" + task.getReward() + " has been added to your wallet for completing: " + task.getTitle(),
                "PAYMENT_RELEASED",
                taskId
        );

        return saved;
    }

    public List<Task> getTasksPostedByUser(Long userId) {
        return taskRepository.findByPosterId(userId);
    }

    public List<Task> getTasksAcceptedByWorker(Long workerId) {
        return taskRepository.findByWorkerId(workerId);
    }

    public List<Task> getTasksByPosterAndStatus(Long posterId, TaskStatus status) {
        return taskRepository.findByPosterIdAndStatus(posterId, status);
    }

    public List<Task> getTasksByWorkerAndStatus(Long workerId, TaskStatus status) {
        return taskRepository.findByWorkerIdAndStatus(workerId, status);
    }

    // Haversine formula for distance in kilometers
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
