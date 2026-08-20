package com.instantwork.repository;

import com.instantwork.model.Task;
import com.instantwork.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPosterId(Long posterId);

    List<Task> findByWorkerId(Long workerId);

    List<Task> findByPosterIdAndStatus(Long posterId, TaskStatus status);

    List<Task> findByWorkerIdAndStatus(Long workerId, TaskStatus status);

    List<Task> findByCategoryIgnoreCase(String category);

    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:category IS NULL OR LOWER(t.category) = LOWER(:category)) AND " +
           "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchTasks(@Param("status") TaskStatus status,
                           @Param("category") String category,
                           @Param("keyword") String keyword);
}
