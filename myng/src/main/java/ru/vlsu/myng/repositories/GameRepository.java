package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Компонент слоя доступа к данным для работы с сущностью Game.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска игр.<br>
 * Используется в следующих сценариях:<br>
 *  - добавление новой игры разработчиком;<br>
 *  - получение списка всех игр разработчика;<br>
 *  - поиск игры по уникальному репозиторию;<br>
 *  - фильтрация игр по жанру;<br>
 *  - проверка существования игры по репозиторию;<br>
 *  - подготовка списка игр для отображения в пользовательском интерфейсе.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface GameRepository extends JpaRepository<Game, Integer>
{

    /**
     * Поиск игры по уникальному репозиторию.
     *
     * @param repo URL или имя репозитория. Не должен быть null или пустым.
     *
     * @return Optional с игрой.
     *         Optional.empty() если игра с указанным репозиторием не найдена.
     *
     * @throws IllegalArgumentException если repo равен null или пустая строка
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<Game> findByRepo(String repo);

    /**
     * Возвращает список всех игр, созданных указанным разработчиком.
     *
     * @param developer пользователь с ролью dev. Не должен быть null.
     *
     * @return список игр. Никогда не возвращает null.
     *         Может быть пустым, если у разработчика ещё нет игр.
     *
     * @throws IllegalArgumentException если developer равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Game> findByDeveloper(User developer);

    /**
     * Возвращает список игр указанного жанра.
     *
     * @param genre жанр игры. Не должен быть null.
     *
     * @return список игр данного жанра. Никогда не возвращает null.
     *         Может быть пустым, если игр с таким жанром нет.
     *
     * @throws IllegalArgumentException если genre равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Game> findByGenre(Game.Genre genre);

    /**
     * Проверяет существование игры по уникальному репозиторию.
     *
     * @param repo URL или имя репозитория. Не должен быть null или пустым.
     *
     * @return true если игра с указанным репозиторием существует, иначе false
     *
     * @throws IllegalArgumentException если repo равен null или пустая строка
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    boolean existsByRepo(String repo);
}