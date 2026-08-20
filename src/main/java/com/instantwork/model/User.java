package com.instantwork.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;

    private String verificationDocType; // AADHAAR, PAN
    private String maskedDocNumber;     // e.g. XXXX-XXXX-1234
    private String nameOnDoc;
    private LocalDateTime verificationSubmittedAt;
    private String verificationRemarks;

    private String location;
    private Double latitude;
    private Double longitude;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_blocked_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "blocked_user_id")
    private Set<Long> blockedUserIds = new HashSet<>();

    private Double rating = 5.0;
    private Integer ratingCount = 0;
    private Integer completedTasks = 0;
    private Double walletBalance = 0.0;
    private Double totalEarned = 0.0;
    private String avatarUrl;

    @Column(length = 1000)
    private String bio;

    private Boolean verified = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String name, String username, String email, String phone, String passwordHash,
                String location, Double latitude, Double longitude, List<String> skills,
                Double rating, Integer ratingCount, Integer completedTasks, Double walletBalance,
                Double totalEarned, String avatarUrl, String bio, Role role,
                VerificationStatus verificationStatus, Boolean verified) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.completedTasks = completedTasks;
        this.walletBalance = walletBalance;
        this.totalEarned = totalEarned;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.role = role != null ? role : Role.USER;
        this.accountStatus = AccountStatus.ACTIVE;
        this.verificationStatus = verificationStatus != null ? verificationStatus : VerificationStatus.NOT_VERIFIED;
        this.verified = verified != null ? verified : false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
        this.verified = (verificationStatus == VerificationStatus.VERIFIED);
    }

    public String getVerificationDocType() { return verificationDocType; }
    public void setVerificationDocType(String verificationDocType) { this.verificationDocType = verificationDocType; }

    public String getMaskedDocNumber() { return maskedDocNumber; }
    public void setMaskedDocNumber(String maskedDocNumber) { this.maskedDocNumber = maskedDocNumber; }

    public String getNameOnDoc() { return nameOnDoc; }
    public void setNameOnDoc(String nameOnDoc) { this.nameOnDoc = nameOnDoc; }

    public LocalDateTime getVerificationSubmittedAt() { return verificationSubmittedAt; }
    public void setVerificationSubmittedAt(LocalDateTime verificationSubmittedAt) { this.verificationSubmittedAt = verificationSubmittedAt; }

    public String getVerificationRemarks() { return verificationRemarks; }
    public void setVerificationRemarks(String verificationRemarks) { this.verificationRemarks = verificationRemarks; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public Set<Long> getBlockedUserIds() { return blockedUserIds; }
    public void setBlockedUserIds(Set<Long> blockedUserIds) { this.blockedUserIds = blockedUserIds; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public Integer getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Integer completedTasks) { this.completedTasks = completedTasks; }

    public Double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }

    public Double getTotalEarned() { return totalEarned; }
    public void setTotalEarned(Double totalEarned) { this.totalEarned = totalEarned; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) {
        this.verified = verified;
        if (verified && this.verificationStatus != VerificationStatus.VERIFIED) {
            this.verificationStatus = VerificationStatus.VERIFIED;
        }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
