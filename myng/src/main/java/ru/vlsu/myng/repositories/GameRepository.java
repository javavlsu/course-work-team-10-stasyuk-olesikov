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

        /**
         * Возвращает список всех игр для отображения в каталоге.
         *
         * <p>
         * Вместе с играми сразу загружаются связанные сущности:
         * developer и tags.
         * Это позволяет избежать проблемы N+1 запросов при работе
         * с каталогом.
         * </p>
         *
         * @return список всех игр каталога.
         *         Никогда не возвращает null.
         *         Может возвращать пустой список, если игры отсутствуют.
         *
         * @throws org.springframework.dao.DataAccessException
         *                                                     при ошибке доступа к базе
         *                                                     данных
         */
        // EntityGraph говорит Hibernate загрузить эти связи сразу (EAGER) одним
        // запросом
        @EntityGraph(attributePaths = { "developer", "tags" })
        @Query("SELECT DISTINCT g FROM Game g")
        List<Game> findAllForCatalog();

        /**
         * Возвращает список игр, отфильтрованных по поисковому запросу
         * и жанру.
         *
         * <p>
         * Поиск выполняется по:
         * </p>
         * <ul>
         *     <li>названию игры;</li>
         *     <li>описанию игры;</li>
         *     <li>имени разработчика.</li>
         * </ul>
         *
         * <p>
         * Если search равен null или пустой строке,
         * текстовый поиск не применяется.
         * </p>
         *
         * <p>
         * Если genre равен null,
         * фильтрация по жанру не применяется.
         * </p>
         *
         * @param search строка поискового запроса.
         *               Может быть null или пустой строкой.
         *
         * @param genre жанр игры.
         *              Может быть null.
         *
         * @return список игр, удовлетворяющих условиям фильтрации.
         *         Никогда не возвращает null.
         *         Может возвращать пустой список, если совпадений нет.
         *
         * @throws org.springframework.dao.DataAccessException
         *                                                     при ошибке доступа к базе
         *                                                     данных
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


    /**
     * Возвращает страницу игр каталога с поддержкой фильтрации
     * и пагинации.
     *
     * <p>
     * В результат включаются только игры, имеющие хотя бы одну
     * подтверждённую модерацией версию.
     * </p>
     *
     * <p>
     * Поддерживаются следующие фильтры:
     * </p>
     * <ul>
     *     <li>поиск по названию, описанию и имени разработчика;</li>
     *     <li>жанр игры;</li>
     *     <li>минимальный рейтинг;</li>
     *     <li>наличие хотя бы одного тега из указанного набора.</li>
     * </ul>
     *
     * <p>
     * Если параметр фильтра равен null
     * (или пустой строке для search),
     * соответствующая фильтрация не применяется.
     * </p>
     *
     * @param search строка поискового запроса.
     *               Может быть null или пустой строкой.
     *
     * @param genre жанр игры.
     *              Может быть null.
     *
     * @param tags набор тегов для фильтрации.
     *             Может быть null.
     *
     * @param minRating минимальный средний рейтинг игры.
     *                  Может быть null.
     *
     * @param pageable параметры пагинации и сортировки.
     *                 Не должен быть null.
     *
     * @return страница DTO игр каталога,
     *         удовлетворяющих условиям фильтрации.
     *         Никогда не возвращает null.
     *
     * @throws IllegalArgumentException                    если pageable равен null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
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
         * Возвращает список наиболее популярных игр,
         * отсортированных по количеству запусков.
         *
         * <p>
         * В результат включаются только игры, имеющие хотя бы одну
         * подтверждённую модерацией версию.
         * </p>
         *
         * <p>
         * Количество возвращаемых игр определяется параметрами pageable.
         * </p>
         *
         * @param pageable параметры пагинации,
         *                 определяющие количество возвращаемых записей.
         *                 Не должен быть null.
         *
         * @return список игр, отсортированных по убыванию количества запусков.
         *         Никогда не возвращает null.
         *         Может возвращать пустой список, если подходящие игры отсутствуют.
         *
         * @throws IllegalArgumentException                    если pageable равен null
         * @throws org.springframework.dao.DataAccessException
         *                                                     при ошибке доступа к базе
         *                                                     данных
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