package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий версий игр.
 */
public interface GameVersionRepository extends JpaRepository<GameVersion, Integer>
{

    /**
     * Получение всех версий игры.
     *
     * @param game игра
     * @return список версий
     */
    List<GameVersion> findByGame(Game game);

    /**
     * Поиск версии по commit hash.
     *
     * @param commitHash хеш коммита
     * @return Optional с версией
     */
    Optional<GameVersion> findByCommitHash(String commitHash);
}