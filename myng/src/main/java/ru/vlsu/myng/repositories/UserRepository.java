package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * Репозиторий для работы с сущностью User.
 * Обеспечивает доступ к данным пользователей.
 */
public interface UserRepository extends JpaRepository<User, Integer>
{

    /**
     * Поиск пользователя по username.
     *
     * @param username имя пользователя, не должно быть null или пустым.
     * @return Optional с найденным пользователем или Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск пользователя по GitHub username.
     *
     * @param githubUsername GitHub username, может быть null.
     * @return Optional с найденным пользователем
     */
    Optional<User> findByGithubUsername(String githubUsername);

    /**
     * Получение списка пользователей по роли.
     *
     * @param role роль пользователя (user, dev, mod, admin)
     * @return список пользователей (может быть пустым, но не null)
     */
    List<User> findByRole(User.Role role);

    /**
     * Проверяет существование пользователя по username.
     *
     * @param username имя пользователя
     * @return true если пользователь существует
     */
    boolean existsByUsername(String username);
}