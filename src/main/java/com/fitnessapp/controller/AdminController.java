package com.fitnessapp.controller;

import com.fitnessapp.dto.UserResponseDTO;
import com.fitnessapp.dto.UserUpdateRequest; // 🌟 НОВ ИМПОРТ: За да използваме UserUpdateRequest
import com.fitnessapp.service.UserService;
import org.slf4j.Logger; // 🌟 НОВ ИМПОРТ: За логиране
import org.slf4j.LoggerFactory; // 🌟 НОВ ИМПОРТ: За логиране
import org.springframework.beans.factory.annotation.Autowired; // 🌟 НОВ ИМПОРТ: За @Autowired
import org.springframework.http.HttpStatus; // 🌟 НОВ ИМПОРТ: За HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException; // 🌟 НОВ ИМПОРТ: За обработка на изключения

@RestController
@RequestMapping("/api/admin") // Базов път за всички административни ендпойнти
@CrossOrigin(origins = "*") // Позволява CORS
// @PreAuthorize("hasRole('ADMIN')") // Можете да го сложите тук, за да важи за всички методи, или индивидуално за всеки метод
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class); // 🌟 ДОБАВЕНО: За логиране

    private final UserService userService;

    @Autowired // 🌟 ДОБАВЕНО: Препоръчително е за конструкторно инжектиране
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Ендпойнт за статистика (ако вече не е в AdminController)
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminStats() { // Променено име за яснота
        logger.info("Получена GET заявка за администраторска статистика.");
        // Можете да направите това динамично, като инжектирате UserRepository и използвате userRepository.count()
        String stats = "Общ брой потребители: (имплементирай брояч); Общ брой режими: (имплементирай брояч); Общ брой рецепти: (имплементирай брояч).";
        return ResponseEntity.ok(stats);
    }

    // 🌟 МОДИФИЦИРАН ЕНДПОЙНТ: За търсене и филтриране
    // Може да се извика с /api/admin/users или /api/admin/users?searchTerm=test
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@RequestParam(required = false) String searchTerm) {
        logger.info("Получена GET заявка за всички потребители (админ панел). SearchTerm: {}", searchTerm);
        List<UserResponseDTO> users = userService.getAllUsersForAdmin(searchTerm); // Извикваме модифицирания метод
        return ResponseEntity.ok(users);
    }

    // Ендпойнт за присвояване на роля
    @PostMapping("/assign-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignRoleToUser(@RequestParam Integer userId, @RequestParam String role) {
        logger.info("Получена заявка за присвояване на роля '{}' на потребител ID: {}", role, userId);
        try {
            userService.assignRole(userId, role);
            return ResponseEntity.ok(String.format("Роля '%s' успешно присвоена на потребител ID %d.", role, userId));
        } catch (NoSuchElementException e) {
            logger.warn("Грешка при присвояване на роля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Грешка при присвояване на роля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Неочаквана грешка при присвояване на роля: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Възникна грешка при присвояване на роля.");
        }
    }

    // 🌟 НОВ ЕНДПОЙНТ: За администраторска редакция на потребител (включително имейл)
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Уверете се, че само администратори могат да го използват
    public ResponseEntity<UserResponseDTO> updateUserData(@PathVariable Integer id, @RequestBody UserUpdateRequest updateRequest) {
        logger.info("Получена PUT заявка за актуализация на потребителски данни от администратор за ID: {}", id);
        try {
            UserResponseDTO updatedUser = userService.updateUserProfile(id, updateRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (NoSuchElementException e) {
            logger.warn("Потребител с ID {} не е намерен за актуализация: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) { // За грешки като зает имейл
            logger.warn("Невалидни данни при актуализация на потребител с ID {}: {}", id, e.getMessage());
            // Може да върнете съобщението за грешка в тялото на отговора
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Връща 400 Bad Request
        } catch (Exception e) {
            logger.error("Неочаквана грешка при актуализация на потребител с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 🌟 ЕНДПОЙНТ за изтриване на потребител
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Уверете се, че само администратори могат да го използват
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        logger.info("Получена DELETE заявка за потребител с ID: {}", id);
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>("Потребител с ID " + id + " е изтрит успешно.", HttpStatus.OK);
        } catch (NoSuchElementException e) { // Хващаме NoSuchElementException, както е променено в UserService
            logger.warn("Грешка при изтриване на потребител с ID {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // Връщаме съобщението от UserService
        } catch (Exception e) {
            logger.error("Неочаквана грешка при изтриване на потребител с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Възникна грешка при изтриване на потребител.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}