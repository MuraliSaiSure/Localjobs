package com.instantwork.service;

import com.instantwork.dto.RatingRequest;
import com.instantwork.model.Review;
import com.instantwork.model.User;
import com.instantwork.repository.ReviewRepository;
import com.instantwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<Review> getReviewsForUser(Long userId) {
        return reviewRepository.findByToUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Review submitReview(RatingRequest request, String taskTitle) {
        User fromUser = userRepository.findById(request.getFromUserId())
                .orElseThrow(() -> new RuntimeException("Reviewer user not found: " + request.getFromUserId()));
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found: " + request.getToUserId()));

        Review review = new Review(
                request.getTaskId(),
                taskTitle,
                fromUser.getId(),
                fromUser.getName(),
                toUser.getId(),
                toUser.getName(),
                request.getRating(),
                request.getReviewText(),
                request.getRole()
        );

        Review saved = reviewRepository.save(review);

        // Update target user's cumulative rating
        int currentCount = toUser.getRatingCount() != null ? toUser.getRatingCount() : 0;
        double currentRating = toUser.getRating() != null ? toUser.getRating() : 5.0;

        double newRating = ((currentRating * currentCount) + request.getRating()) / (currentCount + 1);
        newRating = Math.round(newRating * 10.0) / 10.0; // Round to 1 decimal place

        toUser.setRating(newRating);
        toUser.setRatingCount(currentCount + 1);
        userRepository.save(toUser);

        // Send notification to recipient
        notificationService.createNotification(
                toUser.getId(),
                "New Rating Received ⭐ " + request.getRating(),
                fromUser.getName() + " left you a " + request.getRating() + "★ rating for task: " + taskTitle,
                "NEW_RATING",
                request.getTaskId()
        );

        return saved;
    }
}
