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
     * Регистрирует нового пользователя в системе.
     *
     * <p>
     * Выполняются проверки:
     * <ul>
     *     <li>подтверждение пароля;</li>
     *     <li>минимальная длина пароля (>= 6 символов);</li>
     *     <li>уникальность username;</li>
     *     <li>уникальность email.</li>
     * </ul>
     * </p>
     *
     * <p>
     * При успешной регистрации:
     * <ul>
     *     <li>пароль хешируется;</li>
     *     <li>устанавливается роль user;</li>
     *     <li>задаётся дата регистрации;</li>
     *     <li>создаётся пустой профильный аватар.</li>
     * </ul>
     * </p>
     *
     * @param registrationDto DTO с данными регистрации.
     *                        Не должен быть null.
     *
     * @return созданный пользователь.
     *
     * @throws RuntimeException если данные некорректны
     *                          или пользователь/почта уже существуют
     * @throws org.springframework.dao.DataAccessException
     *                          при ошибке доступа к базе данных
     */
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (!registrationDto.isPasswordConfirmed()) {
            throw new RuntimeException("Пароли не совпадают");
        }

        if (registrationDto.getPassword() == null || registrationDto.getPassword().length() < 6) {
            throw new RuntimeException("Пароль должен быть не менее 6 символов");
        }

        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Имя пользователя уже занято");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());

        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));

        user.setRegisteredAt(Instant.now());

        user.setRole(User.Role.user);

        user.setProfilePic(new byte[0]);

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

    /**
     * Возвращает текущего аутентифицированного пользователя.
     *
     * <p>
     * Извлекает пользователя из Spring Security Context.
     * </p>
     *
     * <p>
     * Поддерживаются только аутентифицированные пользователи
     * с principal типа UserDetails.
     * </p>
     *
     * @return текущий пользователь системы.
     *
     * @throws IllegalStateException если:
     *                                <ul>
     *                                    <li>нет authentication;</li>
     *                                    <li>пользователь не аутентифицирован;</li>
     *                                    <li>principal неизвестного типа;</li>
     *                                    <li>пользователь не найден в базе.</li>
     *                                </ul>
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }

        if (!authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + email));
        }

        throw new IllegalStateException("Unknown authentication principal: " + principal);
    }

    /**
     * Сохранение пользователя.
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Проверяет, является ли текущий пользователь разработчиком.
     *
     * <p>
     * Если пользователь не аутентифицирован,
     * возвращает false.
     * </p>
     *
     * @return true, если текущий пользователь имеет роль dev;
     *         false в остальных случаях.
     */
    public boolean isCurrentUserDev() {
        try {
            User currentUser = getCurrentUser();
            return currentUser != null && currentUser.getRole() == User.Role.dev;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}