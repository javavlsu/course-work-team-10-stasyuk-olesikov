package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью User.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска пользователей.<br>
 * Используется в следующих сценариях:<br>
 *  - регистрация новых пользователей;<br>
 *  - аутентификация и авторизация;<br>
 *  - поиск пользователей по username или GitHub username;<br>
 *  - фильтрация пользователей по роли (user, dev, mod, admin);<br>
 *  - проверка существования username перед созданием нового пользователя;<br>
 *  - подготовка списков пользователей для аналитики и отображения в UI.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Поиск пользователя по username.
     *
     * @param username имя пользователя. Не должно быть null или пустым.
     *
     * @return Optional с найденным пользователем. Optional.empty(), если пользователь не найден.
     *
     * @throws IllegalArgumentException если username равен null или пустой строке
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск пользователя по GitHub username.
     *
     * @param githubUsername GitHub username. Может быть null.
     *
     * @return Optional с найденным пользователем. Optional.empty(), если пользователь не найден или githubUsername равен null.
     *
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<User> findByGithubUsername(String githubUsername);

    /**
     * Возвращает список пользователей с указанной ролью.
     *
     * @param role роль пользователя (user, dev, mod, admin). Не должна быть null.
     *
     * @return список пользователей с указанной ролью. Никогда не возвращает null.
     *         Может быть пустым, если пользователей с такой ролью нет.
     *
     * @throws IllegalArgumentException если role равна null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<User> findByRole(User.Role role);

    /**
     * Проверяет существование пользователя по username.
     *
     * @param username имя пользователя. Не должно быть null или пустым.
     *
     * @return true если пользователь существует, иначе false.
     *
     * @throws IllegalArgumentException если username равен null или пустой строке
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    boolean existsByUsername(String username);
}