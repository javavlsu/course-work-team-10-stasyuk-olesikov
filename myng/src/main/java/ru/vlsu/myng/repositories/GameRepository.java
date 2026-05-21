package ru.vlsu.myng.repositories;

import ru.vlsu.myng.dto.CatalogGameDTO;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Компонент слоя доступа к данным для работы с сущностью Game.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска игр.<br>
 * Используется в следующих сценариях:<br>
 * - добавление новой игры разработчиком;<br>
 * - получение списка всех игр разработчика;<br>
 * - поиск игры по уникальному репозиторию;<br>
 * - фильтрация игр по жанру;<br>
 * - проверка существования игры по репозиторию;<br>
 * - подготовка списка игр для отображения в пользовательском интерфейсе.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface GameRepository extends JpaRepository<Game, Integer> {

        /**
         * Поиск игры по уникальному репозиторию.
         *
         * @param repo URL или имя репозитория. Не должен быть null или пустым.
         *
         * @return Optional с игрой.
         *         Optional.empty() если игра с указанным репозиторием не найдена.
         *
         * @throws IllegalArgumentException                    если repo равен null или
         *                                                     пустая строка
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
         * @throws IllegalArgumentException                    если developer равен null
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
         * @throws IllegalArgumentException                    если genre равен null
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
         * @throws IllegalArgumentException                    если repo равен null или
         *                                                     пустая строка
         * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
         */
        boolean existsByRepo(String repo);

        // EntityGraph говорит Hibernate загрузить эти связи сразу (EAGER) одним
        // запросом
        @EntityGraph(attributePaths = { "developer", "tags" })
        @Query("SELECT DISTINCT g FROM Game g")
        List<Game> findAllForCatalog();

        /**
         * Базовый поиск игр (без тегов и рейтинга)
         */
        @Query("SELECT DISTINCT g FROM Game g " +
                        "LEFT JOIN g.developer d " +
                        "WHERE (:search IS NULL OR :search = '' OR " +
                        "       LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "       LOWER(g.descr) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "       LOWER(d.username) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:genre IS NULL OR g.genre = :genre)")
        List<Game> findGamesWithBasicFilters(@Param("search") String search,
                        @Param("genre") Game.Genre genre);


    @Query("""
    SELECT new ru.vlsu.myng.dto.CatalogGameDTO(
    
        g.id,
        g.name,
        g.descr,
        g.genre,
    
        d.username,
    
        g.averageRating,
        g.reviewCount,
        g.totalViews,
        g.totalLaunches,
        g.firstReleaseDate,
    
        g.image
    )
    
    FROM Game g
    
    LEFT JOIN g.developer d
    
    WHERE
    
    EXISTS (
        SELECT 1
        FROM GameVersion gv
        WHERE gv.game = g
        AND gv.moderationVerdict IS NOT NULL
        AND gv.moderationVerdict.approved = true
    )
    
    AND
    (
        :search IS NULL
        OR :search = ''
        OR LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(g.descr) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(d.username) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    
    AND
    (
        :genre IS NULL
        OR g.genre = :genre
    )
    
    AND
    (
        :minRating IS NULL
        OR g.averageRating >= :minRating
    )
    
    AND
    (
        :tags IS NULL
        OR EXISTS (
            SELECT 1
            FROM g.tags t
            WHERE t.name IN :tags
        )
    )
    """)
    Page<CatalogGameDTO> findCatalogGames(
            @Param("search") String search,
            @Param("genre") Game.Genre genre,
            @Param("tags") Set<String> tags,
            @Param("minRating") Double minRating,
            Pageable pageable
    );
    
        /**
         * Получаем игры отсортированные по колличеству запусков
         * 
         * @param pageable
         * @return
         */
        @Query("""
        SELECT g
        FROM Game g
        WHERE EXISTS (
            SELECT 1
            FROM GameVersion gv
            WHERE gv.game = g
            AND gv.moderationVerdict IS NOT NULL
            AND gv.moderationVerdict.approved = true
        )
        ORDER BY g.totalLaunches DESC
        """)
        List<Game> findTopGamesByLaunches(Pageable pageable);
}