package com.fitnessapp.controller;

import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.dto.LoginResponse;
import com.fitnessapp.dto.RegisterRequest;
import com.fitnessapp.model.User;
import com.fitnessapp.service.AuthService;
import com.fitnessapp.service.GuestService;
import com.fitnessapp.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

// НОВИ ИМПОРТИ ЗА ЗАЯВКИ ЗА ЗАБРАВЕНА/ВЪЗСТАНОВЕНА ПАРОЛА
import com.fitnessapp.request.ForgotPasswordRequest; // Уверете се, че пътят е правилен
import com.fitnessapp.request.ResetPasswordRequest; // Уверете се, че пътят е правилен


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Добре е да ограничите това до конкретни фронтенд домейни за продукция
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final GuestService guestService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest req) {
        log.info("Регистрация – email: {}", req.getEmail());
        try {
            authService.registerUser(req);
            return ResponseEntity.ok("Потребител регистриран успешно!");
        } catch (RuntimeException ex) {
            log.error("Регистрация – грешка: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Регистрация – неочаквана грешка", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Възникна грешка при регистрация.");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("Вход – email: {}", req.getEmail());
        try {
            return ResponseEntity.ok(authService.authenticateUser(req));
        } catch (RuntimeException ex) {
            log.error("Вход – грешка: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception ex) {
            log.error("Вход – неочаквана грешка", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/guest")
    public ResponseEntity<Map<String, String>> guestLogin() {
        //  създаваме гост-потребител
        User guest = guestService.createGuest();

        //  издаваме JWT (24 ч)
        String token = jwtTokenProvider.generateToken(guest, Duration.ofHours(24));

        //  отговаряме
        return ResponseEntity.ok(Map.of(
                "token",  token,
                "userId", guest.getId().toString(),
                "role",   "GUEST"
        ));
    }

    // НОВ ЕНДПОЙНТ: Заявка за забравена парола (изпращане на имейл с линк)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        log.info("Заявка за забравена парола за имейл: {}", forgotPasswordRequest.getEmail());
        try {
            authService.forgotPassword(forgotPasswordRequest.getEmail());
            // Винаги връщаме успешно съобщение за сигурност, за да не подсказваме съществуват ли акаунти
            return ResponseEntity.ok("Ако има акаунт, свързан с този имейл, линк за възстановяване на парола е изпратен.");
        } catch (Exception ex) {
            log.error("Грешка при обработка на заявка за забравена парола за имейл {}: {}", forgotPasswordRequest.getEmail(), ex.getMessage(), ex);
            // Можете да върнете по-общо съобщение за грешка или да игнорирате специфични грешки за сигурност
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Възникна грешка при обработка на заявката. Моля, опитайте отново по-късно.");
        }
    }

    // НОВ ЕНДПОЙНТ: Нулиране на паролата (промяна на паролата чрез токен)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("Заявка за нулиране на парола с токен.");
        try {
            authService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Паролата е успешно променена. Моля, влезте с новата си парола.");
        } catch (IllegalArgumentException ex) { // За невалиден/изтекъл токен
            log.error("Грешка при нулиране на парола (невалиден аргумент): {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Грешка при нулиране на парола: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Възникна грешка при промяна на паролата. Моля, опитайте отново по-късно.");
        }
    }
}