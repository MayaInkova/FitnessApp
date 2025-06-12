package com.fitnessapp.controller;

import com.fitnessapp.dto.UserUpdateRequest;
import com.fitnessapp.model.User;
import com.fitnessapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // Базов път за всички ендпойнти в този контролер
@CrossOrigin(origins = "*") // Разрешаване на CORS


public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // Само за администратори
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Получена GET заявка за всички потребители.");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (#id == authentication.principal.id or hasRole('ADMIN'))") // Само за собственика или ADMIN
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        logger.info("Получена GET заявка за потребител с ID: {}", id);
        User user = userService.getUserById(id);
        if (user == null) {
            logger.warn("Потребител с ID {} не е намерен.", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (#id == authentication.principal.id or hasRole('ADMIN'))") // Само за собственика или ADMIN
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody UserUpdateRequest updateRequest) {
        logger.info("Получена PUT заявка за актуализация на потребител с ID: {}", id);
        try {

            User updatedUser = userService.updateUserProfile(id, updateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Грешка при актуализация на потребител с ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Потребител не е намерен
        } catch (Exception e) {
            logger.error("Неочаквана грешка при актуализация на потребител с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Обикновено само за администратори
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        logger.info("Получена DELETE заявка за потребител с ID: {}", id);
        try {

            return new ResponseEntity<>("Потребител с ID " + id + " е изтрит успешно.", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Грешка при изтриване на потребител с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Грешка при изтриване на потребител.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
