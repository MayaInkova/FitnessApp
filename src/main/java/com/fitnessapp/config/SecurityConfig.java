package com.fitnessapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // позволява използването на @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/chatbot/**",
                                "/api/nutrition-plans/**",
                                "/api/recipes/public/**", // публични рецепти
                                "/api/guest/**",
                                "/api/goals/**",
                                "/api/food-items/**"
                        ).permitAll()

                        // ️ За модератори
                        .requestMatchers("/api/moderator/**")
                        .hasRole("MODERATOR")

                        // 🛡 За администратори
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // 🛡 Всички останали - само ако са логнати
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