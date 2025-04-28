package com.fitnessapp.controller;

import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.model.User;
import com.fitnessapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // За да позволява заявки от фронтенд
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔹 Регистрация на потребител
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Грешка при регистрация: " + e.getMessage());
        }
    }

    // 🔹 Влизане (логване) на потребител
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail());
        if (user == null) {
            return ResponseEntity.status(404).body("Потребителят не съществува!");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Невалидна парола!");
        }

        return ResponseEntity.ok(user);
    }

    // 🔹 Взимане на всички потребители
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 🔹 Взимане на потребител по ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // 🔹 Актуализиране на потребител по ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 Изтриване на потребител по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Потребителят е успешно изтрит.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Грешка при изтриване: " + e.getMessage());
        }
    }
}
