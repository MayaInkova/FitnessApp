package com.fitnessapp.config;

import com.fitnessapp.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Публични ендпойнти, достъпни за всички (неидентифицирани потребители)
                        // Всичко останало под /api/ ще изисква автентикация по-долу.
                        .requestMatchers(
                                "/api/users/register", // Регистрация
                                "/api/users/login",    // Вход
                                "/api/chatbot/**",     // Чатбот (ако е публичен)
                                "/api/guest/**"        // Специфични ендпойнти за гости
                        ).permitAll()

                        // За модератори
                        .requestMatchers("/api/moderator/**")
                        .hasRole("MODERATOR")

                        // За администратори
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Всички останали /api/** ендпойнти изискват автентикация (включително /api/recipes, /api/exercises)
                        .requestMatchers("/api/**").authenticated()

                        // Всички други заявки извън /api/ също изискват автентикация
                        .anyRequest()
                        .authenticated()
                );

        // Добавяме нашия JWT филтър преди филтъра за потребителско име/парола
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Добавяме нашия AuthenticationProvider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}