package ru.vlsu.myng.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
        private final BanFilter banFilter;

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
                                .headers(headers -> headers.frameOptions(f -> f.sameOrigin()))
                                .csrf(csrf -> csrf.disable())
                                .addFilterBefore(banFilter, UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers(
                                                                "/",
                                                                "/auth/**",
                                                                "/games/**",
                                                                "/reviews/more/**",
                                                                "/catalog",
                                                                "/running-game/**",
                                                                "/main.css",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/static/gamefiles/**",
                                                                "/banned",
                                                                "/catalog/tags/**",
                                                                "/favicon.ico",
                                                                "/.well-known/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/profile/**",
                                                                "/profile/*/edit")
                                                .authenticated()

                                                .requestMatchers(
                                                                "/games/new",
                                                                "/games/*/edit",
                                                                "/games/*/versions/new")
                                                .hasAnyRole("DEV", "MOD", "ADMIN")

                                                .requestMatchers(
                                                                "/to_moderate/**")
                                                .hasAnyRole("MOD", "ADMIN")

                                                .requestMatchers(
                                                                "/user-list/**",
                                                                "/moderation_log/**")
                                                .hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/auth")
                                                .loginProcessingUrl("/auth/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/auth?error=true")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/profile/logout")
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                .rememberMe(remember -> remember
                                                .key("myngSecretKey")
                                                .tokenValiditySeconds(86400)
                                                .rememberMeParameter("remember-me"))

                                .userDetailsService(userDetailsService);

                return http.build();
        }
}