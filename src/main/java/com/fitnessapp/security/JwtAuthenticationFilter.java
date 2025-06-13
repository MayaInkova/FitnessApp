package com.fitnessapp.security;

import com.fitnessapp.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Извличане на токена от хедъра на заявката
        String header = request.getHeader("Authorization");
        String jwt = null;
        String userEmail = null;

        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7); // Извличане на самия токен (без "Bearer ")
            try {
                userEmail = jwtTokenProvider.getUsernameFromJWT(jwt);
            } catch (Exception e) {
                // Логване на грешката, като се подава самият обект на изключението 'e'
                logger.error("Невалиден JWT токен или проблем при извличане на имейл.", e);
            }
        }

        // Ако имейл е извлечен и няма текуща автентикация в контекста
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            // Валидиране на токена: Премахнат е аргументът userDetails, за да съответства на сигнатурата
            if (jwtTokenProvider.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Настройване на автентикацията в контекста за сигурност
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Продължаване на филтърната верига
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Логваме пълния URI, за да проверим дали има контекст път
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath(); // Вземаме контекст пътя (ако има такъв)
        String pathWithoutContext = requestURI.substring(contextPath.length()); // Път без контекст

        // Изключваме пътищата, които не изискват JWT автентикация
        boolean shouldSkip = pathWithoutContext.startsWith("/api/auth/");

        if (shouldSkip) {
            logger.info("Skipping JWT authentication for path: {} (Full URI: {})", pathWithoutContext, requestURI);
        } else {
            logger.info("Applying JWT authentication for path: {} (Full URI: {})", pathWithoutContext, requestURI);
        }
        return shouldSkip;
    }
}
