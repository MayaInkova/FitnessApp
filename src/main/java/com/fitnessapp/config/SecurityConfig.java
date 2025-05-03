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
                .csrf(csrf -> csrf.disable()) // изключваме CSRF защита
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/recipes/**").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/register", "/api/nutrition-plans/**","/api/meals/**"
                        ).permitAll()
                        .anyRequest().authenticated() // всички други заявки искат автентикация
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
