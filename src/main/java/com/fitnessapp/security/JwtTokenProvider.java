package com.fitnessapp.security;

import com.fitnessapp.model.User; // Добавен импорт за User
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap; // Добавен импорт
import java.util.Map; // Добавен импорт
import java.util.stream.Collectors; // ***ДОБАВЕН ИМПОРТ: За да използвате Collectors.toList()***

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}") // Взима стойността от application.properties
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}") // Взима стойността от application.properties
    private int jwtExpirationInMs;

    // Генериране на JWT токен
    public String generateToken(Authentication authentication) {
        // Principal е обектът, който представлява логнатия потребител.
        // В нашия случай, ако CustomUserDetailsService връща org.springframework.security.core.userdetails.User,
        // то principal ще е от този тип. Ако връща нашия com.fitnessapp.model.User, то ще е от този тип.
        // За да сме сигурни, че може да достъпим ID-то, ще работим с UserDetails
        // и ще приемем, че UserName (email) и ID са достъпни.

        // Ако CustomUserDetailsService връща нашия модел User:
        User userPrincipal = (User) authentication.getPrincipal(); // Кастваме към нашия User
        Integer userId = userPrincipal.getId(); // Взимаме ID-то
        String username = userPrincipal.getEmail(); // Взимаме имейла

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Добавяме ID-то и ролите като claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId); // Добавяме ID-то
        claims.put("roles", authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList())); // ***КОРИГИРАНО ТУК: Използваме Collectors.toList()***

        return Jwts.builder()
                .setSubject(username) // Субект (username / email)
                .setIssuedAt(new Date()) // Дата на издаване
                .setExpiration(expiryDate) // Дата на изтичане
                .addClaims(claims) // Добавяме нашите claims (id, roles)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Подписване с ключа и алгоритъма
                .compact();
    }


    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            logger.error("Невалиден JWT подпис");
        } catch (MalformedJwtException ex) {
            logger.error("Невалиден JWT токен");
        } catch (ExpiredJwtException ex) {
            logger.error("Изтекъл JWT токен");
        } catch (UnsupportedJwtException ex) {
            logger.error("Неподдържан JWT токен");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string е празен.");
        }
        return false;
    }


    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
