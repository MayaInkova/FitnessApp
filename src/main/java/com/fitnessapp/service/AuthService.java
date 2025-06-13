package com.fitnessapp.service;

import com.fitnessapp.dto.LoginRequest;
import com.fitnessapp.dto.LoginResponse;
import com.fitnessapp.dto.RegisterRequest;
import com.fitnessapp.model.Role; // Добавен импорт
import com.fitnessapp.model.User; // Добавен импорт
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired; // Добавен импорт
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Добавен импорт
import org.springframework.security.core.Authentication; // Добавен импорт
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder; // Добавен импорт
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections; // Добавен импорт
import java.util.List; // Добавен импорт
import java.util.stream.Collectors; // Добавен импорт

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void registerUser(RegisterRequest registerRequest) {
        // Проверка дали имейлът съществува
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Имейлът вече е зает!");
        }

        // Хеширане на паролата
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Създаване на User обект
        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(encodedPassword)
                .build();

        // Задаване на роля по подразбиране (напр. ROLE_USER)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роля 'ROLE_USER' не е намерена."));
        user.setRoles(Collections.singleton(userRole)); // Задаваме колекция с една роля

        // Запазване на потребителя
        userRepository.save(user);
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

        // Извличане на UserDetails за отговора
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Потребител не е намерен след автентикация."));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Връщане на LoginResponse с токена и потребителски данни
        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .accessToken(jwt)
                .tokenType("Bearer")
                .build();
    }
}