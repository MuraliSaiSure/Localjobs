package com.instantwork.service;

import com.instantwork.dto.UserRegistrationRequest;
import com.instantwork.model.User;
import com.instantwork.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            return existing.get();
        }

        String username = request.getName().toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis() % 10000;
        User user = new User(
                request.getName(),
                username,
                request.getEmail(),
                request.getPhone(),
                null,
                request.getLocation() != null ? request.getLocation() : "Ongole",
                request.getLatitude() != null ? request.getLatitude() : 15.5057,
                request.getLongitude() != null ? request.getLongitude() : 80.0499,
                request.getSkills(),
                5.0,
                0,
                0,
                0.0,
                0.0,
                "https://api.dicebear.com/7.x/avataaars/svg?seed=" + request.getName().replaceAll("\\s+", ""),
                request.getBio() != null ? request.getBio() : "Looking for micro-tasks and local work nearby.",
                com.instantwork.model.Role.USER,
                com.instantwork.model.VerificationStatus.VERIFIED,
                true
        );
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String emailOrPhone, String password) {
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return Optional.empty();
        }
        String clean = emailOrPhone.trim();
        Optional<User> byEmail = userRepository.findByEmail(clean);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return userRepository.findAll().stream()
                .filter(u -> (u.getPhone() != null && u.getPhone().contains(clean)) ||
                             (u.getEmail() != null && u.getEmail().equalsIgnoreCase(clean)))
                .findFirst();
    }

    @Transactional
    public User updateUser(Long id, User updatedInfo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (updatedInfo.getName() != null) user.setName(updatedInfo.getName());
        if (updatedInfo.getPhone() != null) user.setPhone(updatedInfo.getPhone());
        if (updatedInfo.getLocation() != null) user.setLocation(updatedInfo.getLocation());
        if (updatedInfo.getLatitude() != null) user.setLatitude(updatedInfo.getLatitude());
        if (updatedInfo.getLongitude() != null) user.setLongitude(updatedInfo.getLongitude());
        if (updatedInfo.getSkills() != null) user.setSkills(updatedInfo.getSkills());
        if (updatedInfo.getBio() != null) user.setBio(updatedInfo.getBio());

        return userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
