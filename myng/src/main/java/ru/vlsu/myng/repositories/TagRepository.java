package ru.vlsu.myng.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vlsu.myng.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Компонент слоя доступа к данным для работы с сущностью Tag.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска тегов.<br>
 * Используется в следующих сценариях:<br>
 * - присвоение тегов играм для фильтрации и поиска;<br>
 * - проверка существования тега перед созданием нового;<br>
 * - отображение списка тегов в UI;<br>
 * - аналитика по популярности тегов.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface TagRepository extends JpaRepository<Tag, Integer> {

    /**
     * Поиск тега по имени.
     *
     * @param name имя тега. Не должно быть null. Формат: только lowercase буквы,
     *             цифры и дефисы (a-z0-9(-)).
     *
     * @return Optional с тегом. Optional.empty() если тег не найден.
     *
     * @throws IllegalArgumentException                    если name равен null или
     *                                                     не соответствует формату
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<Tag> findByName(String name);

    /**
     * Проверяет, существует ли тег с указанным именем.
     *
     * @param name имя тега. Не должно быть null. Формат: только lowercase буквы,
     *             цифры и дефисы (a-z0-9(-)).
     *
     * @return true если тег существует, иначе false.
     *
     * @throws IllegalArgumentException                    если name равен null или
     *                                                     не соответствует формату
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    boolean existsByName(String name);

    /**
     * Возвращает список всех тегов,
     * отсортированных по имени в алфавитном порядке.
     *
     * @return список тегов,
     *         отсортированный по возрастанию имени.
     *         Никогда не возвращает null.
     *         Может возвращать пустой список,
     *         если теги отсутствуют.
     *
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    List<Tag> findAllByOrderByNameAsc();

    /**
     * Возвращает набор имён тегов,
     * связанных с указанной игрой.
     *
     * @param gameId идентификатор игры.
     *               Не должен быть null.
     *
     * @return набор имён тегов игры.
     *         Никогда не возвращает null.
     *         Может возвращать пустой набор,
     *         если у игры отсутствуют теги
     *         или игра не найдена.
     *
     * @throws IllegalArgumentException                    если gameId равен null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    @Query("""
    SELECT t.name
    FROM Game g
    JOIN g.tags t
    WHERE g.id = :gameId
    """)
    Set<String> findTagNamesByGameId(
            @Param("gameId") Integer gameId
    );

    /**
     * Выполняет поиск тегов по части имени.
     *
     * <p>
     * Поиск выполняется без учёта регистра.
     * </p>
     *
     * <p>
     * Результаты сортируются по имени
     * в алфавитном порядке.
     * </p>
     *
     * <p>
     * Количество возвращаемых записей определяется
     * параметрами pageable.
     * </p>
     *
     * @param query строка поискового запроса.
     *              Не должна быть null.
     *
     * @param pageable параметры пагинации,
     *                 определяющие количество возвращаемых записей.
     *                 Не должен быть null.
     *
     * @return список имён тегов,
     *         удовлетворяющих поисковому запросу.
     *         Никогда не возвращает null.
     *         Может возвращать пустой список,
     *         если совпадения отсутствуют.
     *
     * @throws IllegalArgumentException                    если query
     *                                                     или pageable равны null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    @Query("""
    SELECT t.name
    FROM Tag t
    WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY t.name ASC
    """)
    List<String> searchTags(
            @Param("query") String query,
            Pageable pageable
    );
}