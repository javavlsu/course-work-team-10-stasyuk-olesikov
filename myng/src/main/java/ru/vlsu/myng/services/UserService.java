package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.myng.dto.UserRegistrationDto;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.UserRepository;

import java.time.Instant;

/**
 * Сервис для работы с пользователями.
 * Содержит бизнес-логику регистрации, поиска и управления пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрация нового пользователя.
     *
     * @param registrationDto данные из формы регистрации
     * @return зарегистрированный пользователь
     * @throws RuntimeException если:
     *                          - пароли не совпадают
     *                          - username уже занят
     *                          - email уже используется
     *                          - пароль слишком короткий
     */
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // 1. Проверка совпадения паролей
        if (!registrationDto.isPasswordConfirmed()) {
            throw new RuntimeException("Пароли не совпадают");
        }

        // 2. Проверка длины пароля (минимальная безопасность)
        if (registrationDto.getPassword() == null || registrationDto.getPassword().length() < 6) {
            throw new RuntimeException("Пароль должен быть не менее 6 символов");
        }

        // 3. Проверка уникальности username
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Имя пользователя уже занято");
        }

        // 4. Проверка уникальности email
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }

        // 5. Создание нового пользователя
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());

        // Хешируем пароль перед сохранением
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));

        // Устанавливаем дату регистрации
        user.setRegisteredAt(Instant.now());

        // Роль по умолчанию - обычный пользователь
        user.setRole(User.Role.user);

        // profilePic пока пустой (можно установить картинку по умолчанию позже)
        user.setProfilePic(new byte[0]);

        // 6. Сохраняем пользователя
        return userRepository.save(user);
    }

    /**
     * Поиск пользователя по id.
     */
    public User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + id));
    }

    /**
     * Поиск пользователя по username.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
    }

    /**
     * Поиск пользователя по email.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + email));
    }

    /**
     * Проверка существования username.
     */
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Проверка существования email.
     */
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();

            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        }

        throw new IllegalStateException("Unknown authentication principal: " + principal);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}