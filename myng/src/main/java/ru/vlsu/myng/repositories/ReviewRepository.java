package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий отзывов пользователей.
 */
public interface ReviewRepository extends JpaRepository<Review, Integer>
{

    /**
     * Получение отзывов для игры.
     *
     * @param game игра, не должна быть null
     * @return список отзывов
     */
    List<Review> findByGame(Game game);

    /**
     * Получение отзыва пользователя для конкретной игры.
     *
     * @param user пользователь
     * @param game игра
     * @return Optional с отзывом
     */
    Optional<Review> findByUserAndGame(User user, Game game);
}