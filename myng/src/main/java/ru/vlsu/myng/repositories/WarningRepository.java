package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Warning;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Warning.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска предупреждений.<br>
 * Используется в следующих сценариях:<br>
 *  - выдача предупреждений пользователям модераторами;<br>
 *  - просмотр истории предупреждений пользователя;<br>
 *  - отображение предупреждений на UI;<br>
 *  - фильтрация предупреждений по модератору или пользователю для аналитики.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface WarningRepository extends JpaRepository<Warning, Integer> {

    /**
     * Возвращает список всех предупреждений, назначенных указанному пользователю.
     *
     * @param user пользователь. Не должен быть null и должен существовать в базе.
     *
     * @return список предупреждений пользователя. Никогда не возвращает null.
     *         Может быть пустым, если предупреждений нет.
     *
     * @throws IllegalArgumentException если user равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Warning> findByUser(User user);

    /**
     * Возвращает список всех предупреждений, выданных указанным модератором.
     *
     * @param moderator пользователь с ролью mod. Не должен быть null.
     *
     * @return список предупреждений, выданных модератором. Никогда не возвращает null.
     *         Может быть пустым, если модератор не выдавал предупреждений.
     *
     * @throws IllegalArgumentException если moderator равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    List<Warning> findByModerator(User moderator);
}