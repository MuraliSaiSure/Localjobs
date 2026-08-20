package com.instantwork.service;

import com.instantwork.model.Transaction;
import com.instantwork.model.User;
import com.instantwork.repository.TransactionRepository;
import com.instantwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public WalletService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getWalletSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTimestampDesc(userId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("userName", user.getName());
        summary.put("availableBalance", user.getWalletBalance());
        summary.put("totalEarned", user.getTotalEarned());
        summary.put("completedTasks", user.getCompletedTasks());
        summary.put("transactions", transactions);

        return summary;
    }

    public List<Transaction> getTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public Transaction processRewardPayout(Long workerId, Long taskId, String taskTitle, Double reward) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found: " + workerId));

        // Credit to worker's balance
        double currentBalance = worker.getWalletBalance() != null ? worker.getWalletBalance() : 0.0;
        double currentTotal = worker.getTotalEarned() != null ? worker.getTotalEarned() : 0.0;

        worker.setWalletBalance(currentBalance + reward);
        worker.setTotalEarned(currentTotal + reward);
        userRepository.save(worker);

        // Record immutable transaction
        Transaction tx = new Transaction(
                workerId,
                taskId,
                taskTitle,
                reward,
                "CREDIT",
                "Reward released for completing task: " + taskTitle
        );
        return transactionRepository.save(tx);
    }
}
