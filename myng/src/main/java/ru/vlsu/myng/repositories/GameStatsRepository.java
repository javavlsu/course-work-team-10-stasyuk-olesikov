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

    Optional<GameStats> findByGameAndEventTypeAndEventDate(Game game, GameStats.EventType eventType, LocalDate date);

    @Query("SELECT COALESCE(SUM(gs.count), 0) FROM GameStats gs WHERE gs.game = :game AND gs.eventType = :eventType")
    Integer getTotalStatsByGameAndType(@Param("game") Game game, @Param("eventType") GameStats.EventType eventType);

    @Query("SELECT COALESCE(SUM(gs.count), 0) FROM GameStats gs WHERE gs.game = :game AND gs.eventType = :eventType AND gs.eventDate >= :startDate")
    Integer getStatsByGameAndTypeSince(@Param("game") Game game, @Param("eventType") GameStats.EventType eventType,
            @Param("startDate") LocalDate startDate);
}