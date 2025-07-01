package com.fitnessapp.controller;

import com.fitnessapp.dto.UserUpdateRequest;
import com.fitnessapp.dto.UserResponseDTO;
import com.fitnessapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Вероятно вече не е нужно, ако няма getAllUsers
import java.util.NoSuchElementException;
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

    // 💥 ПРЕМАХНАТО: Методът getAllUsers() е преместен в AdminController.java
    // Ако все пак искате да имате getAllUsers тук (което не е препоръчително за обикновени потребители),
    // тогава трябва да извикате userService.getAllUsersForAdmin(null);
    //
    // @GetMapping("/all")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
    //     logger.info("Получена GET заявка за всички потребители.");
    //     List<UserResponseDTO> users = userService.getAllUsersForAdmin(null); // Подаваме null за търсене
    //     return ResponseEntity.ok(users);
    // }

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
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @RequestBody UserUpdateRequest updateRequest) {
        logger.info("Получена PUT заявка за актуализация на потребител с ID: {}", id);
        try {
            UserResponseDTO updatedUser = userService.updateUserProfile(id, updateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) { // Хваща RuntimeException, включително NoSuchElementException и IllegalArgumentException от UserService
            logger.error("Грешка при актуализация на потребител с ID {}: {}", id, e.getMessage());
            // Връщаме 404 ако потребителят не е намерен, или 400 за невалидни данни (напр. зает имейл)
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (e instanceof NoSuchElementException) {
                status = HttpStatus.NOT_FOUND;
            } else if (e instanceof IllegalArgumentException) {
                status = HttpStatus.BAD_REQUEST;
            }
            return ResponseEntity.status(status).body(null); // Можете да върнете и съобщението: .body(e.getMessage())
        } catch (Exception e) {
            logger.error("Неочаквана грешка при актуализация на потребител с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}