package com.fitnessapp.controller;

import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.dto.LoginResponse;
import com.fitnessapp.dto.RegisterRequest;
import com.fitnessapp.service.AuthService; // Уверете се, че AuthService е импортиран
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // За валидация на DTO-та (ако използвате)

@RestController
@RequestMapping("/api/auth") // Базов път за всички ендпойнти в този контролер
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register") // Пълен път ще бъде /api/auth/register
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Получена заявка за регистрация на потребител с имейл: {}", registerRequest.getEmail());
        try {
            authService.registerUser(registerRequest);
            return new ResponseEntity<>("Потребител регистриран успешно!", HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Грешка при регистрация на потребител {}: {}", registerRequest.getEmail(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Напр. ако имейлът е вече зает
        } catch (Exception e) {
            logger.error("Неочаквана грешка при регистрация на потребител {}: {}", registerRequest.getEmail(), e.getMessage(), e);
            return new ResponseEntity<>("Възникна грешка при регистрация.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login") // Пълен път ще бъде /api/auth/login
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Получена заявка за вход на потребител с имейл: {}", loginRequest.getEmail());
        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Грешка при вход на потребител {}: {}", loginRequest.getEmail(), e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED); // За невалидни credential-и
        } catch (Exception e) {
            logger.error("Неочаквана грешка при вход на потребител {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}