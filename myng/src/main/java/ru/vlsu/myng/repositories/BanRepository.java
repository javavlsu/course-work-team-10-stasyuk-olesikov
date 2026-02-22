package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Ban;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Ban.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска банов пользователей.<br>
 * Используется в следующих сценариях:<br>
 * - выдача бана пользователю;<br>
 * - проверка наличия активного бана;<br>
 * - получение истории банов пользователя;<br>
 * - получение списка банов, выданных модератором.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface BanRepository extends JpaRepository<Ban, Integer> {

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
     * @throws IllegalArgumentException                    если user равен null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
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
     * @throws IllegalArgumentException                    если moderator равен null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    List<Ban> findByModerator(User moderator);

    /**
     * Проверяет, существует ли активный бан для пользователя на текущий момент.
     *
     * Активный бан — это бан, у которого startTime ≤ now ≤ endTime.
     *
     * @param userId ID пользователя, для которого проверяется бан. Не должен быть
     *               null.
     * @param now1   текущее время (обычно Instant.now()), для сравнения с
     *               startTime.
     * @param now2   текущее время (обычно Instant.now()), для сравнения с endTime.
     * @return true, если существует активный бан пользователя, иначе false.
     * @throws IllegalArgumentException если userId, now1 или now2 равны null
     */
    boolean existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
            Integer userId,
            Instant now1,
            Instant now2);
}