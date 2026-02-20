package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с играми.
 */
public interface GameRepository extends JpaRepository<Game, Integer>
{

    /**
     * Поиск игры по репозиторию.
     *
     * @param repo URL или имя репозитория, не должно быть null
     * @return Optional с игрой
     */
    Optional<Game> findByRepo(String repo);

    /**
     * Получение игр разработчика.
     *
     * @param developer пользователь с ролью dev
     * @return список игр (может быть пустым)
     */
    List<Game> findByDeveloper(User developer);

    /**
     * Получение игр по жанру.
     *
     * @param genre жанр игры
     * @return список игр
     */
    List<Game> findByGenre(Game.Genre genre);

    boolean existsByRepo(String repo);
}