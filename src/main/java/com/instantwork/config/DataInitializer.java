package com.instantwork.config;

import com.instantwork.model.*;
import com.instantwork.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;

    public DataInitializer(UserRepository userRepository,
                           TaskRepository taskRepository,
                           TransactionRepository transactionRepository,
                           ReviewRepository reviewRepository,
                           NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.transactionRepository = transactionRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Initial Users
        User murali = new User(
                "Murali Sai",
                "murali@localjobs.local",
                "+91 98765 43210",
                "Kurnool Road, Ongole",
                15.5057,
                80.0499,
                Arrays.asList("Java", "Data Entry", "Excel", "Restaurant Operations"),
                4.8,
                18,
                24,
                2450.0,
                8700.0,
                "https://api.dicebear.com/7.x/avataaars/svg?seed=MuraliSai",
                "B.Tech Final Year Student & Local Freelancer. Active poster and reliable task worker in Ongole.",
                true
        );

        User ravi = new User(
                "Ravi Kumar",
                "ravi@localjobs.local",
                "+91 91234 56789",
                "Lawyerpet, Ongole",
                15.5090,
                80.0450,
                Arrays.asList("Delivery", "Packing", "Retail Sales", "Event Management"),
                4.7,
                12,
                15,
                1200.0,
                4500.0,
                "https://api.dicebear.com/7.x/avataaars/svg?seed=RaviKumar",
                "Energetic student looking for flexible weekend & evening part-time micro-gigs.",
                true
        );

        User ananya = new User(
                "Ananya Sharma",
                "ananya@localjobs.local",
                "+91 99887 76655",
                "Trunk Road, Ongole",
                15.5020,
                80.0520,
                Arrays.asList("Tutoring", "Office Management", "Content Writing"),
                4.9,
                22,
                28,
                3100.0,
                11200.0,
                "https://api.dicebear.com/7.x/avataaars/svg?seed=AnanyaSharma",
                "Educator and small business owner offering micro-tasks and tutoring opportunities.",
                true
        );

        userRepository.saveAll(Arrays.asList(murali, ravi, ananya));

        // 2. Create Initial Tasks
        Task task1 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Restaurant Helper",
                "Need an enthusiastic helper to support table service and kitchen assistance during evening dinner rush. Dinner provided.",
                "Restaurants",
                Arrays.asList("Customer Service", "Punctuality", "Basic English/Telugu"),
                400.0,
                "3 hours",
                "Today",
                "6:00 PM",
                "9:00 PM",
                "Grand Kitchen, Trunk Road, Ongole",
                15.5070,
                80.0510
        );

        Task task2 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "Data Entry Assistant",
                "Digitize 50 handwritten customer order sheets into MS Excel with accuracy. Clean office workspace with AC.",
                "Data Entry",
                Arrays.asList("Excel", "Fast Typing", "Attention to Detail"),
                300.0,
                "2 hours",
                "Today",
                "2:00 PM",
                "4:00 PM",
                "Sharma Associates, Lawyerpet, Ongole",
                15.5085,
                80.0465
        );

        Task task3 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "Event Helper & Guest Coordinator",
                "Assist event organizers with registration desk, stage arrangements, and gift packet distribution for college symposium.",
                "Events",
                Arrays.asList("Communication", "Coordination", "Active"),
                800.0,
                "1 day",
                "Tomorrow",
                "9:00 AM",
                "6:00 PM",
                "RIMS Convention Hall, Ongole",
                15.5150,
                80.0600
        );

        Task task4 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Shop Sales Assistant",
                "Assist retail customers with apparel selection and manage billing counter support during festival discount sale.",
                "Retail",
                Arrays.asList("Retail Sales", "Friendly Demeanor"),
                500.0,
                "4 hours",
                "Today",
                "4:00 PM",
                "8:00 PM",
                "Trends Clothing, Kurnool Road, Ongole",
                15.5040,
                80.0480
        );

        Task task5 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "College Lab Assistant",
                "Help configure and verify 20 desktop computers for upcoming online competitive exam session.",
                "Education",
                Arrays.asList("Basic Hardware", "Network Basics"),
                450.0,
                "3 hours",
                "Tomorrow",
                "9:00 AM",
                "12:00 PM",
                "PACE Institute Lab 3, Ongole",
                15.5120,
                80.0550
        );

        Task task6 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Warehouse Packing & Labeling Worker",
                "Assist in boxing and barcode labeling electronic accessory packages for dispatch.",
                "Warehouse",
                Arrays.asList("Packing", "Speed", "Organization"),
                600.0,
                "4 hours",
                "Today",
                "1:00 PM",
                "5:00 PM",
                "Logistics Hub, Bypass Road, Ongole",
                15.5180,
                80.0410
        );

        Task task7 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "Office Document Organizer",
                "Sort, index, and neatly file client case folders in archive cabinets.",
                "Office",
                Arrays.asList("Filing", "Organization"),
                350.0,
                "2 hours",
                "Tomorrow",
                "11:00 AM",
                "1:00 PM",
                "Apex Legal Consultancy, Ongole",
                15.5060,
                80.0490
        );

        // Pre-completed task to demonstrate history and reviews
        Task task8 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Flyer Distribution & Marketing Assistant",
                "Hand out promotional store opening flyers to passers-by near municipal stadium.",
                "Sales",
                Arrays.asList("Active", "Friendly"),
                350.0,
                "2 hours",
                "Yesterday",
                "10:00 AM",
                "12:00 PM",
                "Municipal Stadium Road, Ongole",
                15.5030,
                80.0470
        );
        task8.setWorkerId(ravi.getId());
        task8.setWorkerName(ravi.getName());
        task8.setStatus(TaskStatus.PAYMENT_RELEASED);
        task8.setCompletedAt(LocalDateTime.now().minusDays(1));

        taskRepository.saveAll(Arrays.asList(task1, task2, task3, task4, task5, task6, task7, task8));

        // 3. Transactions for Murali and Ravi
        Transaction tx1 = new Transaction(
                murali.getId(), task8.getId(), task8.getTitle(), 350.0, "DEBIT", "Reward paid for completed task: " + task8.getTitle()
        );
        Transaction tx2 = new Transaction(
                ravi.getId(), task8.getId(), task8.getTitle(), 350.0, "CREDIT", "Reward received for completing task: " + task8.getTitle()
        );
        Transaction tx3 = new Transaction(
                murali.getId(), null, "Weekend Cash Bonus", 500.0, "CREDIT", "Top Rated Task Poster Performance Bonus"
        );
        transactionRepository.saveAll(Arrays.asList(tx1, tx2, tx3));

        // 4. Initial Reviews
        Review review1 = new Review(
                task8.getId(), task8.getTitle(), murali.getId(), murali.getName(), ravi.getId(), ravi.getName(),
                5.0, "Ravi was punctual, energetic, and completed the flyer distribution very professionally! Highly recommended.",
                "POSTER_RATING_WORKER"
        );
        Review review2 = new Review(
                task8.getId(), task8.getTitle(), ravi.getId(), ravi.getName(), murali.getId(), murali.getName(),
                5.0, "Great experience working with Murali! Clear instructions, friendly guidance, and payment released immediately.",
                "WORKER_RATING_POSTER"
        );
        reviewRepository.saveAll(Arrays.asList(review1, review2));

        // 5. Initial Notifications
        Notification notif1 = new Notification(
                murali.getId(),
                "Payment Released Successfully 💰",
                "You released ₹350 to Ravi Kumar for 'Flyer Distribution & Marketing Assistant'.",
                "PAYMENT_RELEASED",
                task8.getId()
        );
        Notification notif2 = new Notification(
                murali.getId(),
                "New 5★ Review Received ⭐",
                "Ravi Kumar left you a glowing 5-star review!",
                "NEW_RATING",
                task8.getId()
        );
        notificationRepository.saveAll(Arrays.asList(notif1, notif2));
    }
}
