package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Компонент слоя доступа к данным для работы с сущностью Review.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска отзывов пользователей.<br>
 * Используется в следующих сценариях:<br>
 *  - пользователь оставляет отзыв на игру;<br>
 *  - проверка наличия существующего отзыва пользователя для игры;<br>
 *  - отображение отзывов игры на странице игры;<br>
 *  - аналитика по рейтингам и количеству отзывов;<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository и поддерживает динамические предикаты через JpaSpecificationExecutor.
 */
public interface ReviewRepository extends JpaRepository<Review, Integer>, JpaSpecificationExecutor<Review> {

    /**
     * Возвращает список всех отзывов для указанной игры.
     *
     * @param game игра. Не должна быть null.
     *
     * @return список отзывов игры. Никогда не возвращает null.
     *         Может быть пустым, если отзывов ещё нет.
     *
     * @throws IllegalArgumentException если game равна null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Review> findByGame(Game game);

    /**
     * Возвращает отзыв пользователя для конкретной игры, если он существует.
     *
     * @param user пользователь. Не должен быть null.
     * @param game игра. Не должна быть null.
     *
     * @return Optional с отзывом. Optional.empty(), если отзыв отсутствует.
     *
     * @throws IllegalArgumentException если user или game равны null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<Review> findByUserAndGame(User user, Game game);

    /**
     * Проверяет, существует ли отзыв пользователя для игры.
     *
     * @param user пользователь. Не должен быть null.
     * @param game игра. Не должна быть null.
     *
     * @return true, если отзыв существует, иначе false.
     *
     * @throws IllegalArgumentException если user или game равны null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    boolean existsByUserAndGame(User user, Game game);

    /**
     * Возвращает список отзывов указанного пользователя.
     *
     * @param user пользователь. Не должен быть null.
     *
     * @return список отзывов пользователя. Никогда не возвращает null.
     *         Может быть пустым, если отзывов нет.
     *
     * @throws IllegalArgumentException если user равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Review> findByUser(User user);
}