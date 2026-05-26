package ru.vlsu.myng.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.GameStats;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameStatsRepository extends JpaRepository<GameStats, Integer> {

    /**
     * Возвращает статистику игры по указанному типу события и дате.
     *
     * <p>
     * Метод выполняет поиск записи статистики,
     * соответствующей конкретной игре,
     * типу события и дате.
     * </p>
     *
     * @param game игра, для которой выполняется поиск статистики.
     *             Не должна быть null.
     *             Должна быть персистентной сущностью (id != null).
     *
     * @param eventType тип события статистики.
     *                  Не должен быть null.
     *
     * @param date дата события.
     *             Не должна быть null.
     *
     * @return Optional с найденной записью статистики.
     *         Возвращает Optional.empty(),
     *         если запись не найдена.
     *
     * @throws IllegalArgumentException                    если game,
     *                                                     eventType
     *                                                     или date равны null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    Optional<GameStats> findByGameAndEventTypeAndEventDate(
            Game game,
            GameStats.EventType eventType,
            LocalDate date
    );


    /**
     * Возвращает общее количество событий указанного типа для игры.
     *
     * <p>
     * Если статистические записи отсутствуют,
     * возвращается 0.
     * </p>
     *
     * @param game игра, для которой вычисляется статистика.
     *             Не должна быть null.
     *             Должна быть персистентной сущностью (id != null).
     *
     * @param eventType тип события статистики.
     *                  Не должен быть null.
     *
     * @return суммарное количество событий указанного типа.
     *         Никогда не возвращает null.
     *
     * @throws IllegalArgumentException                    если game
     *                                                     или eventType равны null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    @Query("SELECT COALESCE(SUM(gs.count), 0) FROM GameStats gs WHERE gs.game = :game AND gs.eventType = :eventType")
    Integer getTotalStatsByGameAndType(@Param("game") Game game,
                                       @Param("eventType") GameStats.EventType eventType);


    /**
     * Возвращает суммарное количество событий указанного типа
     * для игры, начиная с указанной даты.
     *
     * <p>
     * В расчёт включаются только записи статистики,
     * дата которых больше или равна startDate.
     * </p>
     *
     * <p>
     * Если статистические записи отсутствуют,
     * возвращается 0.
     * </p>
     *
     * @param game игра, для которой вычисляется статистика.
     *             Не должна быть null.
     *             Должна быть персистентной сущностью (id != null).
     *
     * @param eventType тип события статистики.
     *                  Не должен быть null.
     *
     * @param startDate дата начала периода.
     *                  Не должна быть null.
     *
     * @return суммарное количество событий указанного типа
     *         за период, начиная с startDate.
     *         Никогда не возвращает null.
     *
     * @throws IllegalArgumentException                    если game,
     *                                                     eventType
     *                                                     или startDate равны null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    @Query("SELECT COALESCE(SUM(gs.count), 0) FROM GameStats gs WHERE gs.game = :game AND gs.eventType = :eventType AND gs.eventDate >= :startDate")
    Integer getStatsByGameAndTypeSince(@Param("game") Game game,
                                       @Param("eventType") GameStats.EventType eventType,
                                       @Param("startDate") LocalDate startDate);
}