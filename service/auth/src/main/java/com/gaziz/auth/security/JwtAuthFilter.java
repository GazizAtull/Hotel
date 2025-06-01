package com.gaziz.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для обработки JWT-токена в каждом запросе.
 * 1. Читает заголовок Authorization.
 * 2. Проверяет префикс "Bearer ".
 * 3. Извлекает токен и валидирует через JwtTokenProvider.
 * 4. Если токен валидный, загружает UserDetails и
 *    устанавливает Authentication в SecurityContext.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider,
                         UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. Получаем заголовок Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 2. Проверяем, что заголовок начинается с "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            // 3. Извлекаем username из токена (если валиден)
            username = jwtTokenProvider.getUsernameFromToken(token);
        }

        // 4. Если получили username и текущий контекст не аутентифицирован
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 5. Загружаем UserDetails по имени пользователя
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 6. Проверяем валидность токена относительно UserDetails
            if (jwtTokenProvider.validateToken(token, userDetails)) {
                // 7. Создаём объект аутентификации
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 8. Устанавливаем аутентификацию в контекст
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Передаём управление дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Здесь можно исключить некоторые пути из проверки JWT, например:
        // return request.getServletPath().startsWith("/api/auth/");
        return false;
    }
}
