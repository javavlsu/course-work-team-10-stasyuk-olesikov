package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий пользовательских коллекций.
 */
public interface CollectionRepository extends JpaRepository<Collection, Integer>
{

    /**
     * Получение коллекций пользователя.
     *
     * @param user владелец коллекции
     * @return список коллекций
     */
    List<Collection> findByUser(User user);
}