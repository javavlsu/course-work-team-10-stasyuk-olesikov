package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Notification;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Notification.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска уведомлений.<br>
 * Используется в следующих сценариях:<br>
 * - отправка системных уведомлений пользователям;<br>
 * - отображение уведомлений в личном кабинете;<br>
 * - фильтрация уведомлений по типу (system, warning, moderation, news);<br>
 * - подготовка списка уведомлений для аналитики.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Возвращает список всех уведомлений указанного типа.
     *
     * @param type тип уведомления. Не должен быть null.
     *
     * @return список уведомлений. Никогда не возвращает null.
     *         Может быть пустым, если уведомлений данного типа нет.
     *
     * @throws IllegalArgumentException                    если type равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Notification> findByType(Notification.Type type);

    /**
     * Возвращает список уведомлений для указанного пользователя.
     * <p>
     * Метод использует прямую связь через коллекцию {@code users} в сущности
     * Notification.
     * Выполняет поиск по объекту User, что требует предварительной загрузки
     * пользователя из БД.
     * </p>
     *
     * @param user пользователь, для которого выполняется поиск уведомлений.
     *             Не должен быть null. Должен быть персистентной сущностью (id !=
     *             null).
     *
     * @return список уведомлений пользователя. Никогда не возвращает null.
     *         Может быть пустым, если уведомлений для пользователя нет.
     *
     * @throws IllegalArgumentException                    если user равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Notification> findByUsers(User user);

    /**
     * Возвращает список уведомлений для пользователя по его ID.
     * <p>
     * Оптимизированный метод, выполняющий поиск уведомлений напрямую по ID
     * пользователя
     * с использованием JPQL запроса. Не требует предварительной загрузки объекта
     * User.
     * Результат сортируется по дате создания от новых к старым.
     * </p>
     *
     * @param userId ID пользователя, для которого выполняется поиск уведомлений.
     *               Не должен быть null. Должен существовать в базе данных.
     *
     * @return список уведомлений пользователя, отсортированный по убыванию даты
     *         создания.
     *         Никогда не возвращает null. Может быть пустым, если уведомлений для
     *         пользователя нет.
     *
     * @throws IllegalArgumentException                    если userId равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    @Query("SELECT n FROM Notification n JOIN n.users u WHERE u.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findUserNotifications(@Param("userId") Integer userId);
}