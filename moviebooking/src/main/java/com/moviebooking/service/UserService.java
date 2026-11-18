package com.moviebooking.service;

import com.moviebooking.entity.User;
import com.moviebooking.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class UserService {

    private final UserRepository userRepository;

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register a new user
    @Transactional
    public User register(String name, String email, String password) {

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();

        user.setName(name);
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(hashPassword(password));

        return userRepository.save(user);
    }

    // Login an existing user
    public User login(String email, String password) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                    new RuntimeException("Invalid email or password"));

        if (!verifyPassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    // Generate a secure password hash
    private String hashPassword(String password) {

        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            return "pbkdf2$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt)
                    + "$"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    // Verify the entered password
    private boolean verifyPassword(String password, String storedPassword) {

        try {
            String[] parts = storedPassword.split("\\$");

            if (parts.length != 4 || !parts[0].equals("pbkdf2")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] originalHash = Base64.getDecoder().decode(parts[3]);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    iterations,
                    originalHash.length * 8
            );

            byte[] enteredHash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            return MessageDigest.isEqual(originalHash, enteredHash);

        } catch (Exception e) {
            return false;
        }
    }
}