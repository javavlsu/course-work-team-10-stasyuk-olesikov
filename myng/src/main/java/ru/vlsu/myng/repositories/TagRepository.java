package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Компонент слоя доступа к данным для работы с сущностью Tag.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска тегов.<br>
 * Используется в следующих сценариях:<br>
 *  - присвоение тегов играм для фильтрации и поиска;<br>
 *  - проверка существования тега перед созданием нового;<br>
 *  - отображение списка тегов в UI;<br>
 *  - аналитика по популярности тегов.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface TagRepository extends JpaRepository<Tag, Integer> {

    /**
     * Поиск тега по имени.
     *
     * @param name имя тега. Не должно быть null. Формат: только lowercase буквы, цифры и дефисы (a-z0-9(-)).
     *
     * @return Optional с тегом. Optional.empty() если тег не найден.
     *
     * @throws IllegalArgumentException если name равен null или не соответствует формату
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    Optional<Tag> findByName(String name);

    /**
     * Проверяет, существует ли тег с указанным именем.
     *
     * @param name имя тега. Не должно быть null. Формат: только lowercase буквы, цифры и дефисы (a-z0-9(-)).
     *
     * @return true если тег существует, иначе false.
     *
     * @throws IllegalArgumentException если name равен null или не соответствует формату
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    boolean existsByName(String name);
}