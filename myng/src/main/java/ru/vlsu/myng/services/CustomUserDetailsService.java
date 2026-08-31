package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.Collections;

/**
 * Реализация {@link UserDetailsService} для аутентификации пользователей
 * через Spring Security.<br>
 * <br>
 * Обеспечивает загрузку данных пользователя по логину,
 * который может быть как email, так и username.<br>
 * Используется в следующих сценариях:<br>
 * - аутентификация пользователя при входе в систему;<br>
 * - проверка учётных данных при каждом запросе к защищённым ресурсам;<br>
 * - преобразование сущности {@link User} в объект {@link UserDetails}
 *   для Spring Security.<br>
 * <br>
 * Поиск пользователя выполняется в два этапа:<br>
 * <ol>
 *   <li>поиск по email (основной способ входа);</li>
 *   <li>если пользователь не найден по email —
 *       поиск по username (альтернативный способ входа).</li>
 * </ol>
 * <br>
 * Роль пользователя преобразуется в GrantedAuthority с префиксом "ROLE_",
 * что соответствует соглашениям Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по логину для аутентификации.
     *
     * <p>
     * Логином может выступать:
     * </p>
     * <ul>
     *   <li>email пользователя — основной способ входа;</li>
     *   <li>username пользователя — альтернативный способ входа.</li>
     * </ul>
     *
     * <p>
     * Алгоритм поиска:
     * </p>
     * <ol>
     *   <li>Выполняется поиск пользователя по email.</li>
     *   <li>Если пользователь не найден по email,
     *       выполняется поиск по username.</li>
     *   <li>Если пользователь не найден ни по email, ни по username,
     *       выбрасывается {@link UsernameNotFoundException}.</li>
     * </ol>
     *
     * <p>
     * В качестве username для Spring Security используется email
     * найденного пользователя. Это обеспечивает единообразие
     * идентификации пользователя в системе безопасности.
     * </p>
     *
     * <p>
     * Роль пользователя преобразуется в полномочие Spring Security
     * с префиксом "ROLE_" в верхнем регистре
     * (например, "ROLE_USER", "ROLE_DEV", "ROLE_MOD", "ROLE_ADMIN").
     * </p>
     *
     * @param login логин пользователя (email или username).
     *              Не должен быть null или пустым.
     *
     * @return объект {@link UserDetails} с данными пользователя,
     *         где:
     *         <ul>
     *           <li>username — email пользователя;</li>
     *           <li>password — хеш пароля из БД;</li>
     *           <li>authorities — список с одной ролью пользователя.</li>
     *         </ul>
     *
     * @throws UsernameNotFoundException если пользователь не найден
     *         ни по email, ни по username
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login)
                .orElse(null);

        if (user == null) {
            user = userRepository.findByUsername(login)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Пользователь не найден с логином: " + login));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase())));
    }
}