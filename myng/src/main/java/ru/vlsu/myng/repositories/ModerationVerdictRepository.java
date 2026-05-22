package ru.vlsu.myng.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.DevApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Компонент слоя доступа к данным для работы с сущностью ModerationVerdict.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска решений модерации.<br>
 * Используется в следующих сценариях:<br>
 *  - модератор выносит решение по заявкам разработчиков;<br>
 *  - модератор выносит решение по версиям игр;<br>
 *  - модератор выносит решение по жалобам на отзывы;<br>
 *  - получение списка решений модерации по трем типам сущностей для аналитики или отображения в UI.<br>
 *  - получение списков трёх видов сущностей, не прошедших модерацию
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface ModerationVerdictRepository extends JpaRepository<ModerationVerdict, Integer>
{

    /**
     * Возвращает список всех решений модерации, вынесенных указанным модератором.
     *
     * @param moderator пользователь с ролью mod. Не должен быть null.
     *
     * @return список решений модерации. Никогда не возвращает null.
     *         Может быть пустым, если модератор ещё не вынес ни одного решения.
     *
     * @throws IllegalArgumentException если moderator равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<ModerationVerdict> findByModerator(User moderator);

    /**
     * Возвращает список всех решений модерации по указанной версии игры.
     *
     * @param version версия игры. Не должна быть null.
     *
     * @return список решений модерации. Никогда не возвращает null.
     *         Может быть пустым, если по версии ещё нет решений.
     *
     * @throws IllegalArgumentException если version равна null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<ModerationVerdict> findByGameVersion(GameVersion version);

    /**
     * Возвращает список всех решений модерации по указанному отзыву.
     *
     * @param review отзыв. Не должен быть null.
     *
     * @return список решений модерации. Никогда не возвращает null.
     *         Может быть пустым, если по отзыву ещё нет решений.
     *
     * @throws IllegalArgumentException если review равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<ModerationVerdict> findByReview(Review review);

    /**
     * Возвращает решение модерации по заявке разработчика.
     *
     * @param application заявка на роль разработчика. Не должна быть null.
     *
     * @return Optional с решением модерации.
     *         Optional.empty() если решение по заявке ещё не вынесено.
     *
     * @throws IllegalArgumentException если application равна null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<ModerationVerdict> findByDevApplication(DevApplication application);

    /**
     * Возвращает список всех версий игр, по которым ещё не вынесено решение модерации.
     *
     * @return список решений модерации по версиям игр с флагом approved = false.
     *         Никогда не возвращает null, может быть пустым, если таких версий нет.
     *
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<ModerationVerdict> findByGameVersionIsNotNullAndApprovedIsNull();

    /**
     * Возвращает список всех отзывов, по которым ещё не вынесено решение модерации.
     *
     * @return список решений модерации по отзывам с флагом approved = false.
     *         Никогда не возвращает null, может быть пустым, если таких отзывов нет.
     *
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<ModerationVerdict> findByReviewIsNotNullAndApprovedIsNull();

    /**
     * Возвращает список всех заявок разработчиков, по которым ещё не вынесено решение модерации.
     *
     * @return список решений модерации по заявкам разработчиков с флагом approved = false.
     *         Никогда не возвращает null, может быть пустым, если таких заявок нет.
     *
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<ModerationVerdict> findByDevApplicationIsNotNullAndApprovedIsNull();

    boolean existsByReview(Review review);

    @Query("""
    SELECT new ru.vlsu.myng.dto.ModerationItem(

        mv.id,

        COALESCE(gv.id, da.id, r.id),

        CASE
            WHEN gv IS NOT NULL THEN 'GAME_VERSION'
            WHEN da IS NOT NULL THEN 'DEV_APPLICATION'
            WHEN r IS NOT NULL THEN 'REVIEW'
        END,

        CASE
            WHEN gv IS NOT NULL THEN gv.game.id
            WHEN r IS NOT NULL THEN r.game.id
            ELSE NULL
        END,

        u.username,
        da.githubUsername,
        da.text,

        gv.commitHash,
        gv.changelog,
        g.repo,

        CAST(r.rating as integer),
        r.text,
        r.reportCount,

        COALESCE(
            gv.createdAt,
            da.createdAt,
            r.createdAt
        ),

        mod.username,
        mv.approved,
        mv.reason
    )

    FROM ModerationVerdict mv

    LEFT JOIN mv.gameVersion gv
    LEFT JOIN gv.game g

    LEFT JOIN mv.devApplication da
    LEFT JOIN da.user u

    LEFT JOIN mv.review r

    LEFT JOIN mv.moderator mod

    WHERE

    (
        :search IS NULL
        OR :search = ''

        OR LOWER(mod.username) LIKE LOWER(CONCAT('%', :search, '%'))

        OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))

        OR LOWER(gv.commitHash) LIKE LOWER(CONCAT('%', :search, '%'))
        
        OR LOWER(mv.reason) LIKE LOWER(CONCAT('%', :search, '%'))

        OR CAST(COALESCE(gv.id, da.id, r.id) as string)
            LIKE CONCAT('%', :search, '%')
    )

    AND
    (
        :type IS NULL
        OR :type = ''

        OR (
            :type = 'GAME_VERSION'
            AND gv IS NOT NULL
        )

        OR (
            :type = 'DEV_APPLICATION'
            AND da IS NOT NULL
        )

        OR (
            :type = 'REVIEW'
            AND r IS NOT NULL
        )
    )

    AND
    (
        :status IS NULL
        OR :status = ''

        OR (
            :status = 'approved'
            AND mv.approved = true
        )

        OR (
            :status = 'rejected'
            AND mv.approved = false
        )
        
        OR (
            :status = 'pending'
            AND mv.approved IS NULL
        )
    )

    AND
    (
        :createdAfter IS NULL
    
        OR COALESCE(
            gv.createdAt,
            da.createdAt,
            r.createdAt
        ) >= :createdAfter
    )
    """)
    Page<ModerationItem> getModerationItems(
            String search,
            String type,
            String status,
            Instant createdAfter,
            Pageable pageable
    );
}