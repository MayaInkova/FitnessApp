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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
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
}