package com.fitnessapp.controller;

import com.fitnessapp.dto.UserUpdateRequest;
import com.fitnessapp.dto.UserResponseDTO;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")


public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        logger.info("Получена GET заявка за всички потребители.");
        List<UserResponseDTO> users = userService.getAllUsersDTO();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (#id == authentication.principal.id or hasRole('ADMIN'))")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        logger.info("Получена GET заявка за потребител с ID: {}", id);
        Optional<UserResponseDTO> userDTO = userService.getUserByIdDTO(id);
        if (userDTO.isEmpty()) {
            logger.warn("Потребител с ID {} не е намерен.", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDTO.get());
    }


    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (#id == authentication.principal.id or hasRole('ADMIN'))")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @RequestBody UserUpdateRequest updateRequest) { // Променен тип на връщане на DTO
        logger.info("Получена PUT заявка за актуализация на потребител с ID: {}", id);
        try {

            UserResponseDTO updatedUser = userService.updateUserProfile(id, updateRequest);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        logger.info("Получена DELETE заявка за потребител с ID: {}", id);
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>("Потребител с ID " + id + " е изтрит успешно.", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Грешка при изтриване на потребител с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Грешка при изтриване на потребител.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
