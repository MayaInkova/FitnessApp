package com.fitnessapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // Import за SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // позволява използването на @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Деактивиране на CSRF за stateless API
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Важно за REST API с JWT
                .authorizeHttpRequests(auth -> auth
                        // Публични ендпойнти, достъпни за всички (неидентифицирани потребители)
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/chatbot/**", 
                                "/api/guest/**",
                                "/api/recipes/public/**", // Пример: публични рецепти
                                "/api/exercises/public/**" // Пример: публични упражнения
                        ).permitAll()

                        // За модератори
                        .requestMatchers("/api/moderator/**")
                        .hasRole("MODERATOR")

                        // За администратори
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Всички останали /api/** ендпойнти, които НЕ са изброени по-горе, изискват автентикация
                        // ПРИМЕР: /api/users/profile, /api/nutrition-plans, /api/training, etc.
                        .requestMatchers("/api/**").authenticated() // Всички /api/ пътища, които не са permitAll, изискват автентикация

                        // Всички други заявки извън /api/ също изискват автентикация,
                        // освен ако не са изрично позволени по-горе
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}