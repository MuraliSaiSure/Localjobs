package com.instantwork.config;

import com.instantwork.model.*;
import com.instantwork.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository,
                           TaskRepository taskRepository,
                           TransactionRepository transactionRepository,
                           ReviewRepository reviewRepository,
                           NotificationRepository notificationRepository,
                           ReportRepository reportRepository,
                           AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.transactionRepository = transactionRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
        this.reportRepository = reportRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        String defaultPassHash = passwordEncoder.encode("password123");
        String adminPassHash = passwordEncoder.encode("Admin@123");

        // 0. Create Admin User (Securely seeded, no public admin signup)
        User admin = new User(
                "System Administrator",
                "admin",
                "admin@localjobs.local",
                "+91 99999 00000",
                adminPassHash,
                "Platform Headquarters",
                15.5057,
                80.0499,
                Arrays.asList("Platform Governance", "Moderation", "Security"),
                5.0,
                0,
                0,
                0.0,
                0.0,
                "https://api.dicebear.com/7.x/bottts/svg?seed=AdminLocalJobs",
                "Official LocalJobs Platform Administrator & Trust Safety Officer.",
                Role.ADMIN,
                VerificationStatus.VERIFIED,
                true
        );
        userRepository.save(admin);

        // 1. Create Verified Demo Users
        User murali = new User(
                "Murali Sai",
                "muralisai",
                "murali@localjobs.local",
                "+91 98765 43210",
                defaultPassHash,
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
                Role.USER,
                VerificationStatus.VERIFIED,
                true
        );
        murali.setVerificationDocType("AADHAAR");
        murali.setMaskedDocNumber("XXXX-XXXX-4321");
        murali.setNameOnDoc("Murali Sai Sure");

        User ravi = new User(
                "Ravi Kumar",
                "ravikumar",
                "ravi@localjobs.local",
                "+91 91234 56789",
                defaultPassHash,
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
                Role.USER,
                VerificationStatus.VERIFIED,
                true
        );
        ravi.setVerificationDocType("AADHAAR");
        ravi.setMaskedDocNumber("XXXX-XXXX-9876");
        ravi.setNameOnDoc("Ravi Kumar K");

        User ananya = new User(
                "Ananya Sharma",
                "ananyasharma",
                "ananya@localjobs.local",
                "+91 99887 76655",
                defaultPassHash,
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
                Role.USER,
                VerificationStatus.VERIFIED,
                true
        );
        ananya.setVerificationDocType("PAN");
        ananya.setMaskedDocNumber("XXXXX8765X");
        ananya.setNameOnDoc("Ananya Sharma");

        // Demo User Pending Verification
        User suresh = new User(
                "Suresh Varma",
                "suresh_v",
                "suresh@localjobs.local",
                "+91 98888 11111",
                defaultPassHash,
                "Collectorate Area, Ongole",
                15.5075,
                80.0480,
                Arrays.asList("Graphic Design", "Photography"),
                5.0,
                0,
                0,
                0.0,
                0.0,
                "https://api.dicebear.com/7.x/avataaars/svg?seed=SureshVarma",
                "Creative freelancer. Submitted identity verification.",
                Role.USER,
                VerificationStatus.VERIFICATION_PENDING,
                false
        );
        suresh.setVerificationDocType("AADHAAR");
        suresh.setMaskedDocNumber("XXXX-XXXX-5544");
        suresh.setNameOnDoc("Suresh Varma");
        suresh.setVerificationSubmittedAt(LocalDateTime.now().minusHours(2));

        userRepository.saveAll(Arrays.asList(murali, ravi, ananya, suresh));

        // 2. Initial Active Tasks
        Task task1 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Restaurant Helper - Dinner Rush",
                "Need an energetic helper for food assembly, table clearing, and dish support during peak dinner rush.",
                "Restaurants",
                Arrays.asList("Food Service", "Punctual", "Fast Paced"),
                400.0, "3 hours", "Today", "6:00 PM", "9:00 PM",
                "Grand Kitchen, Trunk Road, Ongole", 15.5035, 80.0510
        );

        Task task2 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "Retail Shop Inventory & Barcode Scanning",
                "Scan new inventory garments, attach price tags, and organize storage racks.",
                "Retail & Shops",
                Arrays.asList("Barcode Scanner", "Attention to Detail"),
                350.0, "2.5 hours", "Tomorrow", "10:00 AM", "12:30 PM",
                "Trendz Fashion Mall, Lawyerpet, Ongole", 15.5085, 80.0460
        );

        Task task3 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "College Symposium Registration Desk Support",
                "Manage attendee check-in, distribute event kits, and provide venue directions.",
                "Events",
                Arrays.asList("Communication", "English/Telugu", "Friendly"),
                500.0, "4 hours (Half Day)", "Saturday", "8:30 AM", "12:30 PM",
                "PACE Institute Campus, Ongole", 15.5130, 80.0580
        );

        Task task4 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "Office Document Scanning & Digital Archiving",
                "Scan legal & invoice files into high-resolution PDFs and organize into Google Drive folders.",
                "Office",
                Arrays.asList("Scanner Operation", "PDF Tools", "Confidentiality"),
                300.0, "2 hours", "Today", "3:00 PM", "5:00 PM",
                "Sharma Consultancy, Court Road, Ongole", 15.5060, 80.0475
        );

        Task task5 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Computer Lab OS Update & Network Verification",
                "Verify Ethernet connectivity and run system update scripts across 25 desktop workstations.",
                "IT & Software",
                Arrays.asList("Windows 11", "Basic Networking", "Troubleshooting"),
                600.0, "3 hours", "Friday", "2:00 PM", "5:00 PM",
                "Apex Computer Academy, Santhapeta, Ongole", 15.5010, 80.0540
        );

        Task task6 = new Task(
                ananya.getId(), ananya.getName(), ananya.getRating(), ananya.getCompletedTasks(),
                "Product Catalog Data Entry in Excel",
                "Transcribe supplier invoices and product SKU details into our standardized Excel sheet.",
                "Data Entry",
                Arrays.asList("MS Excel", "Typing 40+ WPM", "Accuracy"),
                450.0, "3 hours", "Today", "Flexible", "Flexible",
                "Online / Near RIMS Hospital, Ongole", 15.5095, 80.0515
        );

        Task task7 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Hyperlocal Urgent Document Delivery",
                "Pick up stamped blueprints from architect office and deliver to builder site.",
                "Delivery",
                Arrays.asList("Two Wheeler", "Smartphone with GPS"),
                200.0, "45 minutes", "Today", "4:30 PM", "5:15 PM",
                "From Trunk Road to South Bypass, Ongole", 15.5040, 80.0505
        );

        Task task8 = new Task(
                murali.getId(), murali.getName(), murali.getRating(), murali.getCompletedTasks(),
                "Flyer Distribution & Marketing Assistant",
                "Distribute new opening promo brochures to nearby shops and residential streets.",
                "Sales & Marketing",
                Arrays.asList("Active", "Local Area Knowledge"),
                350.0, "2 hours", "Yesterday", "10:00 AM", "12:00 PM",
                "Gandhi Park Market Area, Ongole", 15.5070, 80.0490
        );
        task8.setWorkerId(ravi.getId());
        task8.setWorkerName(ravi.getName());
        task8.setStatus(TaskStatus.PAYMENT_RELEASED);

        taskRepository.saveAll(Arrays.asList(task1, task2, task3, task4, task5, task6, task7, task8));

        // 3. Initial Transactions
        Transaction tx1 = new Transaction(
                murali.getId(), null, "Wallet Deposit via UPI", 3000.0, "CREDIT", "Loaded funds to post local tasks"
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
                ravi.getId(),
                "₹350 Credited to Wallet! 🎉",
                "Murali Sai released ₹350 for completing 'Flyer Distribution & Marketing Assistant'.",
                "PAYMENT_RELEASED",
                task8.getId()
        );
        notificationRepository.saveAll(Arrays.asList(notif1, notif2));

        // 6. Initial Incident Report (For Admin Review Demo)
        Report sampleReport = new Report(
                ananya.getId(),
                ananya.getName(),
                null,
                null,
                task1.getId(),
                task1.getTitle(),
                "TASK_REPORT",
                "Misleading information",
                "Task location description needed slight clarification regarding landmark."
        );
        reportRepository.save(sampleReport);

        // 7. Initial Admin Audit Logs
        AuditLog log1 = new AuditLog("admin", "SYSTEM_INITIALIZED", "SYSTEM", 1L, "LocalJobs Platform", "Seeded platform initial verified personas and security policies");
        AuditLog log2 = new AuditLog("admin", "VERIFICATION_APPROVED", "USER_VERIFICATION", murali.getId(), murali.getName(), "Approved Aadhaar mock KYC verification");
        AuditLog log3 = new AuditLog("admin", "VERIFICATION_APPROVED", "USER_VERIFICATION", ravi.getId(), ravi.getName(), "Approved Aadhaar mock KYC verification");
        auditLogRepository.saveAll(Arrays.asList(log1, log2, log3));
    }
}
