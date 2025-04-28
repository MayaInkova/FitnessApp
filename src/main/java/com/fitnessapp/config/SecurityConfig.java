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
                .csrf(csrf -> csrf.disable()) // изключваме CSRF засега
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register").permitAll() // позволяваме регистрацията без токен
                        .anyRequest().authenticated() // всичко друго иска автентикация
                );
        return http.build();
    }
}