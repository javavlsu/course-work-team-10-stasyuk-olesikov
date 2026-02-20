package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Ban;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий банов пользователей.
 */
public interface BanRepository extends JpaRepository<Ban, Integer>
{

    /**
     * Получение банов пользователя.
     *
     * @param user пользователь
     * @return список банов
     */
    List<Ban> findByUser(User user);
}