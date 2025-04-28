package com.fitnessapp.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Изключваме CSRF за улеснение при тестове
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll() // Позволяваме достъп до login и register
                        .anyRequest().authenticated() // Всичко друго изисква логин
                )
                .formLogin(form -> form.disable()); // Изключваме дефолтния login form на Spring

        return http.build();
    }
}