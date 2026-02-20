package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Warning;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий предупреждений.
 */
public interface WarningRepository extends JpaRepository<Warning, Integer>
{
    List<Warning> findByUser(User user);
}