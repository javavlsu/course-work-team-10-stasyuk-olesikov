package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Ban;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Ban.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска банов пользователей.<br>
 * Используется в следующих сценариях:<br>
 *  - выдача бана пользователю;<br>
 *  - проверка наличия активного бана;<br>
 *  - получение истории банов пользователя;<br>
 *  - получение списка банов, выданных модератором.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface BanRepository extends JpaRepository<Ban, Integer>
{

    /**
     * Возвращает список банов, назначенных указанному пользователю.
     *
     * @param user пользователь, для которого выполняется поиск.
     *             Не должен быть null.
     *             Должен быть персистентной сущностью (id != null).
     *
     * @return список банов пользователя.
     *         Никогда не возвращает null.
     *         Может возвращать пустой список, если баны отсутствуют.
     *
     * @throws IllegalArgumentException если user равен null
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    List<Ban> findByUser(User user);

    /**
     * Возвращает список банов, назначенных указанным модератором.
     *
     * @param moderator пользователь с ролью модератора, который назначал баны.
     *                  Не должен быть null.
     *                  Должен быть персистентной сущностью (id != null).
     *
     * @return список банов, выданных данным модератором.
     *         Никогда не возвращает null.
     *         Может возвращать пустой список, если модератор ещё не назначал банов.
     *
     * @throws IllegalArgumentException если moderator равен null
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    List<Ban> findByModerator(User moderator);
}