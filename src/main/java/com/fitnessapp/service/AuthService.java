package com.fitnessapp.service;

import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.dto.LoginResponse;
import com.fitnessapp.dto.RegisterRequest;
import com.fitnessapp.model.Role;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import за @Transactional

import java.time.LocalDateTime; // Import за LocalDateTime
import java.util.Collections;
import java.util.List;
import java.util.Optional; // Import за Optional
import java.util.UUID; // Import за UUID (за токен)
import java.util.stream.Collectors;
import org.slf4j.Logger; // Import за Logger
import org.slf4j.LoggerFactory; // Import за LoggerFactory


@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class); // Инициализация на логер

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;


    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    @Transactional // Важно за операции с база данни, като запазване на потребител
    public void registerUser(RegisterRequest registerRequest) {
        // Проверка дали имейлът съществува
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Имейлът '" + registerRequest.getEmail() + "' вече е зает!"); // По-конкретно съобщение
        }

        // Хеширане на паролата
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Създаване на User обект
        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(encodedPassword)
                // Новите полета за възстановяване на парола не се задават при регистрация
                .build();


        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роля 'ROLE_USER' не е намерена в базата данни."));
        user.setRoles(Collections.singleton(userRole));


        userRepository.save(user);
        log.info("Потребител '{}' успешно регистриран.", user.getEmail());
    }

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        // Извършване на автентикация
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Задаване на автентикацията в SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генериране на JWT токен
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Извличане на User за отговора (потребителят е автентикиран, така че вече го има)
        User user = (User) authentication.getPrincipal(); // Може да получите потребителя директно от authentication

        Optional<User> authenticatedUserOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (authenticatedUserOptional.isEmpty()) {
            throw new RuntimeException("Потребител не е намерен след успешна автентикация.");
        }
        User authenticatedUser = authenticatedUserOptional.get();


        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Връщане на LoginResponse с токена и потребителски данни
        return LoginResponse.builder()
                .id(authenticatedUser.getId()) // Използваме id от намерений потребител
                .email(authenticatedUser.getEmail())
                .roles(roles)
                .accessToken(jwt)
                .tokenType("Bearer")
                .build();
    }

    //  Забравена парола - генерира токен и изпраща имейл
    @Transactional
    public void forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // За сигурност, не казвайте на клиента дали имейлът съществува или не.
            // Просто логнете и излезте, за да предотвратите изброяване на потребителски имена.
            log.warn("Заявка за забравена парола за несъществуващ имейл: {}", email);
            return; // Връщаме се без грешка, фронтендът ще получи "успех"
        }

        User user = userOptional.get();

        // Генериране на уникален токен
        String token = UUID.randomUUID().toString();
        // Задаване на токена и валидност (например 24 часа от сега)
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(24)); // Токенът е валиден за 24 часа

        userRepository.save(user); // Запазваме токена и срока му в базата данни


        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        String subject = "Заявка за възстановяване на парола за Fitness App";
        String text = "Здравейте " + user.getFullName() + ",\n\n"
                + "Получихме заявка за възстановяване на паролата за вашия акаунт.\n"
                + "Моля, кликнете на следния линк, за да зададете нова парола:\n"
                + resetLink + "\n\n"
                + "Този линк е валиден за 24 часа.\n"
                + "Ако не сте правили тази заявка, моля игнорирайте този имейл.\n\n"
                + "Поздрави,\n"
                + "Екипът на Fitness App";

        emailService.sendEmail(user.getEmail(), subject, text);
        log.info("Линк за възстановяване на парола изпратен до: {}", user.getEmail());
    }

    //  Нулиране на парола - валидира токен и сменя парола
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);

        if (userOptional.isEmpty()) {
            log.warn("Опит за нулиране на парола с невалиден или липсващ токен: {}", token);
            throw new IllegalArgumentException("Невалиден или изтекъл токен за възстановяване на парола.");
        }

        User user = userOptional.get();

        // Проверка дали токенът не е изтекъл
        if (user.getResetPasswordTokenExpiryDate() == null || user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            // Изчистваме токена, ако е изтекъл, за да не може да се използва отново
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiryDate(null);
            userRepository.save(user); // Запазваме промените
            log.warn("Опит за нулиране на парола с изтекъл токен за потребител: {}", user.getEmail());
            throw new IllegalArgumentException("Токенът за възстановяване на парола е изтекъл. Моля, поискайте нов.");
        }

        // Обновяване на паролата
        user.setPassword(passwordEncoder.encode(newPassword));

        // Изчистване на токена след употреба
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiryDate(null);

        userRepository.save(user); // Запазваме обновената парола и изчистените токени
        log.info("Паролата на потребител '{}' е успешно променена.", user.getEmail());
    }
}