package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.ModerationVerdict;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий решений модерации.
 */
public interface ModerationVerdictRepository extends JpaRepository<ModerationVerdict, Integer>
{
}