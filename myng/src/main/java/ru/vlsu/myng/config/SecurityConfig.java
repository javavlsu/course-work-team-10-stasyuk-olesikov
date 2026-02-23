package ru.vlsu.myng.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения.
 * Содержит настройки шифрования паролей и другие компоненты безопасности.
 */
@Configuration
public class SecurityConfig {

    /**
     * Создает бин для шифрования паролей.
     *
     * @return PasswordEncoder для шифрования паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // РАЗРЕШАЕМ ВСЕ СТРАНИЦЫ (временно)
                        .anyRequest().permitAll())
                // ОТКЛЮЧАЕМ автоматическую форму логина
                .formLogin(form -> form.disable())
                // ОТКЛЮЧАЕМ защиту от CSRF для простоты (пока)
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}