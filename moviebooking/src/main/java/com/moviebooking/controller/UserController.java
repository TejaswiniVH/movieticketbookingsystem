package com.moviebooking.controller;

import com.moviebooking.entity.User;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
    origins = "http://127.0.0.1:5500",
    allowCredentials = "true"
)
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(
            UserService userService,
            UserRepository userRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
    }

    // USER REGISTRATION
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (user.getName() == null ||
                user.getName().trim().length() < 2) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Enter a valid name."));
        }

        if (user.getEmail() == null ||
                !user.getEmail().matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Enter a valid email."));
        }

        if (user.getPassword() == null ||
                user.getPassword().length() < 6) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Password must contain at least 6 characters."
                    ));
        }

        User savedUser = userService.register(
                user.getName(),
                user.getEmail(),
                user.getPassword()
        );

        return ResponseEntity.ok(
                createUserResponse(savedUser, "Registration successful")
        );
    }


    // USER LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User loginUser,
            HttpSession session) {

        if (loginUser.getEmail() == null ||
                loginUser.getPassword() == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email and password are required."
                    ));
        }

        User user = userService.login(
                loginUser.getEmail(),
                loginUser.getPassword()
        );

        if (user == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email or password."
                    ));
        }

        session.setAttribute("userId", user.getId());

        return ResponseEntity.ok(
                createUserResponse(user, "Login successful")
        );
    }


    // CHECK CURRENT LOGIN SESSION
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {

            session.invalidate();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found."));
        }

        return ResponseEntity.ok(
                createUserResponse(user, "Session active")
        );
    }


    // USER LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logout successful.")
        );
    }


    // CREATE RESPONSE WITHOUT PASSWORD
    private Map<String, Object> createUserResponse(
            User user,
            String message) {

        Map<String, Object> response = new HashMap<>();

        response.put("message", message);
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return response;
    }
}