package com.instantwork.repository;

import com.instantwork.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    List<Review> findByTaskId(Long taskId);
}
