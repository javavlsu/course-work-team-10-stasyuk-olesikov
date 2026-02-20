package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Collection.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска пользовательских коллекций.<br>
 * Используется в следующих сценариях:<br>
 *  - создание новой коллекции пользователем;<br>
 *  - получение всех коллекций конкретного пользователя;<br>
 *  - редактирование названия коллекции;<br>
 *  - удаление коллекции.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface CollectionRepository extends JpaRepository<Collection, Integer>
{

    /**
     * Возвращает список коллекций, принадлежащих указанному пользователю.
     *
     * @param user владелец коллекции.
     *             Не должен быть null.
     *             Должен быть персистентной сущностью (id != null).
     *
     * @return список коллекций пользователя.
     *         Никогда не возвращает null.
     *         Может возвращать пустой список, если у пользователя нет коллекций.
     *
     * @throws IllegalArgumentException если user равен null
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    List<Collection> findByUser(User user);
}