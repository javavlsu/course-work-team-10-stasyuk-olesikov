package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Notification;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Notification.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска уведомлений.<br>
 * Используется в следующих сценариях:<br>
 *  - отправка системных уведомлений пользователям;<br>
 *  - отображение уведомлений в личном кабинете;<br>
 *  - фильтрация уведомлений по типу (system, warning, moderation, news);<br>
 *  - подготовка списка уведомлений для аналитики.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface NotificationRepository extends JpaRepository<Notification, Integer>
{

    /**
     * Возвращает список всех уведомлений указанного типа.
     *
     * @param type тип уведомления. Не должен быть null.
     *
     * @return список уведомлений. Никогда не возвращает null.
     *         Может быть пустым, если уведомлений данного типа нет.
     *
     * @throws IllegalArgumentException если type равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Notification> findByType(Notification.Type type);

    /**
     * Возвращает список уведомлений для указанного пользователя.
     *
     * @param user пользователь. Не должен быть null.
     *
     * @return список уведомлений пользователя. Никогда не возвращает null.
     *         Может быть пустым, если уведомлений для пользователя нет.
     *
     * @throws IllegalArgumentException если user равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Notification> findByUsers(User user);
}