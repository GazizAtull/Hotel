package com.gaziz.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Расширенная конфигурация OpenAPI (Swagger) для Auth Service.
 * Здесь задано:
 * - Информация о проекте (Info, Contact, License)
 * - Серверы (Dev/Prod)
 * - Теги для группировки эндпоинтов
 * - SecurityScheme для JWT Bearer
 * - Глобальное требование безопасности
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                // 1. Информация о API
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0.0")
                        .description("REST API для аутентификации и управления пользователями")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Support Team")
                                .email("support@your-company.example.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                // 2. Серверы развертывания
                .servers(List.of(
                        new Server().url("http://localhost:8081")
                                .description("Локальный (Dev) сервер Auth Service"),
                        new Server().url("https://api.your-company.example.com/auth")
                                .description("Production сервер Auth Service")
                ))
                // 3. Теги для группировки конечных точек
                .tags(List.of(
                        new Tag().name("Authentication").description("Эндпоинты для логина/регистрации"),
                        new Tag().name("User Management").description("Управление профилями пользователей"),
                        new Tag().name("Health Check").description("Проверка состояния сервиса")
                ))
                // 4. Определения компонентов (схем безопасности и др.)
                .components(new Components()
                        // 4.1. Security Scheme для JWT Bearer
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Используйте формат: Bearer <ваш_JWT_токен>"))
                )
                // 5. Глобальное требование безопасности: все методы требуют BearerAuth (если не переопределено)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
