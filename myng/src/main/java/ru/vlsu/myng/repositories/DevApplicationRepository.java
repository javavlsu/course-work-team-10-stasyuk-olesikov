package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Компонент слоя доступа к данным для работы с сущностью DevApplication.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска заявок на получение роли разработчика.<br>
 * Используется в следующих сценариях:<br>
 *  - пользователь подаёт заявку на роль разработчика;<br>
 *  - проверка наличия активной заявки пользователя;<br>
 *  - получение всех заявок для модерации;<br>
 *  - обработка решения модератора по заявке.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface DevApplicationRepository extends JpaRepository<DevApplication, Integer>
{

    /**
     * Возвращает заявку на разработчика, поданную указанным пользователем.
     *
     * @param user пользователь, подавший заявку.
     *             Не должен быть null.
     *             Должен быть персистентной сущностью (id != null).
     *
     * @return List с заявками пользователя.
     *         Никогда не возвращает null.
     *         Пустой список если пользователь ещё не подавал заявку.
     *
     * @throws IllegalArgumentException если user равен null
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    List<DevApplication> findByUser(User user);
}