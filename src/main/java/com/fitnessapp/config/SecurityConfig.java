package com.fitnessapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Изключваме CSRF за тестване с Postman
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/recipes/**",
                                "/api/users/login",
                                "/api/users/register",
                                "/api/nutrition-plans/**",
                                "/api/meals/**",
                                "/api/chatbot/**",
                                "/api/goals/**","/api/meal-items/**",
                                "/api/food-items/**",
                                "/api/recipe-ingredients/**",
                                "/api/chat-sessions/**",
                                "/api/chat-messages/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}