package ru.vlsu.myng.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import ru.vlsu.myng.services.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения.
 * Содержит настройки шифрования паролей и другие компоненты безопасности.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;

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
                    // ВРЕМЕННО отключаем CSRF (только для разработки!)
                    .headers(headers -> headers.frameOptions(f -> f.sameOrigin()))
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(authz -> authz
                                    // 1. ПУБЛИЧНЫЕ СТРАНИЦЫ (доступны всем)
                                    .requestMatchers(
                                                    "/", // главная
                                                    "/auth/**", // вход/регистрация
                                                    "/games/**", // просмотр игр
                                                    "/catalog", // список игр
                                                    "/running-game/**", // игра
                                                    "/main.css", // стили
                                                    "/js/**", // скрипты
                                                    "/images/**", // картинки
                                                    "/static/gamefiles/**" // файлы игр
                                    ).permitAll()

                                    // 2. СТРАНИЦЫ ДЛЯ АВТОРИЗОВАННЫХ (user, dev, mod, admin)
                                    .requestMatchers(
                                                    "/profile/**", // личный кабинет
                                                    "/profile/*/edit")
                                    .authenticated()

                                    // 3. СТРАНИЦЫ ДЛЯ РАЗРАБОТЧИКОВ (dev, mod, admin)
                                    .requestMatchers(
                                                    "/games/new", // создание игры
                                                    "/games/*/edit", // редактирование игры
                                                    "/games/*/versions/new" // добавление версии
                                    ).hasAnyRole("DEV", "MOD", "ADMIN")

                                    // 4. СТРАНИЦЫ ДЛЯ МОДЕРАТОРОВ (mod, admin)
                                    .requestMatchers(
                                                    "/to_moderate/**" // панель модератора
                                    ).hasAnyRole("MOD", "ADMIN")

                                    // 5. СТРАНИЦЫ ТОЛЬКО ДЛЯ АДМИНОВ
                                    .requestMatchers(
                                                    "/user-list/**", // управление пользователями
                                                    "/moderation_log/**" // лог модераций
                                    ).hasRole("ADMIN")

                                    // ВСЕ ОСТАЛЬНЫЕ СТРАНИЦЫ - только авторизованным
                                    .anyRequest().authenticated())

                    // НАСТРОЙКА ВХОДА
                    .formLogin(form -> form
                                    .loginPage("/auth") // страница с формой
                                    .loginProcessingUrl("/auth/login") // куда отправлять форму
                                    .defaultSuccessUrl("/") // куда после успеха
                                    .failureUrl("/auth?error=true") // куда при ошибке
                                    .usernameParameter("username") // поле для username или почты
                                    .passwordParameter("password") // поле для пароля
                                    .permitAll())

                    // НАСТРОЙКА ВЫХОДА
                    .logout(logout -> logout
                                    .logoutUrl("/profile/logout")
                                    .logoutSuccessUrl("/")
                                    .invalidateHttpSession(true)
                                    .deleteCookies("JSESSIONID")
                                    .permitAll())

                    // ЗАПОМИНАНИЕ ПОЛЬЗОВАТЕЛЯ (remember-me)
                    .rememberMe(remember -> remember
                                    .key("myngSecretKey")
                                    .tokenValiditySeconds(86400) // 24 часа
                                    .rememberMeParameter("remember-me"))

                    // Говорим Spring Security использовать наш UserDetailsService
                    .userDetailsService(userDetailsService);

            return http.build();
        }
}