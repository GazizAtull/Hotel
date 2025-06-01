package com.gaziz.auth.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * Провайдер для генерации и валидации JWT-токенов.
 * В продакшен-окружении значения secret и expirationMillis приходят из Config Server.
 */
@Component
public class JwtTokenProvider {

    /**
     * Секретная фраза (должна быть достаточно длинной и храниться в Config Server или секретах).
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Время жизни токена в миллисекундах (например, 86400000 = 24 часа).
     */
    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMillis;

    /**
     * Ключ, сформированный из jwtSecret для подписи токенов.
     */
    private Key signingKey;

    @PostConstruct
    public void init() {
        // Создаём HMAC SHA-256 ключ из строки jwtSecret
        // В продакшене убедитесь, что jwtSecret достаточно длинная (не менее 256 бит).
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Генерирует JWT-токен для переданного UserDetails.
     *
     * @param userDetails объект UserDetails с именем пользователя (subject).
     * @return подписанный JWT-токен.
     */
    public String generateToken(@NonNull UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMillis);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает имя пользователя (subject) из JWT-токена.
     *
     * @param token JWT-токен.
     * @return username (subject) из токена, либо null, если не удалось распарсить.
     */
    public String getUsernameFromToken(@NonNull String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException ex) {
            // Если токен недействителен (просрочен, изменен, неверный формат), возвращаем null
            return null;
        }
    }

    /**
     * Проверяет, что токен действителен и соответствует переданному UserDetails.
     *
     * @param token       JWT-токен.
     * @param userDetails объект UserDetails.
     * @return true, если токен валиден и subject совпадает с userDetails.getUsername(), иначе false.
     */
    public boolean validateToken(@NonNull String token, @NonNull UserDetails userDetails) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();
            Date now = new Date();

            return (username.equals(userDetails.getUsername()) && expiration.after(now));
        } catch (ExpiredJwtException ex) {
            // Токен просрочен
            return false;
        } catch (JwtException ex) {
            // MalformedJwtException, SignatureException, UnsupportedJwtException и т.д.
            return false;
        }
    }
}
