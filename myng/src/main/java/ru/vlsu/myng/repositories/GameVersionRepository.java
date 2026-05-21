package ru.vlsu.myng.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Компонент слоя доступа к данным для работы с сущностью GameVersion.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска версий игр.<br>
 * Используется в следующих сценариях:<br>
 * - добавление новой версии игры разработчиком;<br>
 * - получение всех версий конкретной игры;<br>
 * - поиск версии по commit hash для модерации или отката;<br>
 * - подготовка списка версий для отображения в UI;<br>
 * - проверка существования версии по commit hash.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface GameVersionRepository extends JpaRepository<GameVersion, Integer> {

    /**
     * Возвращает список всех версий указанной игры.
     *
     * @param game игра, для которой ищутся версии.
     *             Не должен быть null.
     *             Должен быть персистентной сущностью (id != null).
     *
     * @return список версий игры. Никогда не возвращает null.
     *         Может быть пустым, если у игры ещё нет версий.
     *
     * @throws IllegalArgumentException                    если game равна null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к базе
     *                                                     данных
     */
    List<GameVersion> findByGame(Game game);

    /**
     * Поиск версии игры по commit hash.
     *
     * @param commitHash хеш коммита. Не должен быть null или пустым.
     *
     * @return Optional с версией игры.
     *         Optional.empty() если версия с указанным commit hash не найдена.
     *
     * @throws IllegalArgumentException                    если commitHash равен
     *                                                     null или пустая строка
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к базе
     *                                                     данных
     */
    Optional<GameVersion> findByCommitHash(String commitHash);

    Optional<GameVersion> findFirstByGameOrderByCreatedAtAsc(Game game);

    /**
     * Находим самую последнюю версию среди всех игр, которая имеет хотя бы одну
     * подтвежденную версию
     *
     * @return
     */
    GameVersion findFirstByOrderByCreatedAtDesc();

    List<GameVersion> findByGameIdOrderByCreatedAtAsc(Integer gameId);

    @Query("""
            SELECT gv
            FROM GameVersion gv
            WHERE gv.moderationVerdict IS NOT NULL
            AND gv.moderationVerdict.approved = true
            ORDER BY gv.createdAt DESC
            """)
    List<GameVersion> findLatestApproved(Pageable pageable);
}