package com.fitnessapp.security;

import com.fitnessapp.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationInMs;


    public String generateToken(Authentication authentication) {

        User user = (User) authentication.getPrincipal(); // principal = нашия User
        return buildToken(user, Duration.ofMillis(jwtExpirationInMs));
    }

    public String generateToken(User user, Duration expiresIn) {
        return buildToken(user, expiresIn);
    }


    private String buildToken(User user, Duration duration) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + duration.toMillis());

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("roles", user.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .addClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
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