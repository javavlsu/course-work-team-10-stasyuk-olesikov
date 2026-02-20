package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для тегов.
 */
public interface TagRepository extends JpaRepository<Tag, Integer>
{

    /**
     * Поиск тега по имени.
     *
     * @param name имя тега (lowercase, формат a-z0-9(-))
     * @return Optional с тегом
     */
    Optional<Tag> findByName(String name);

    boolean existsByName(String name);
}