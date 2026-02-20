package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий заявок на разработчика.
 */
public interface DevApplicationRepository extends JpaRepository<DevApplication, Integer>
{

    /**
     * Получение заявки пользователя.
     *
     * @param user пользователь
     * @return Optional с заявкой
     */
    Optional<DevApplication> findByUser(User user);
}