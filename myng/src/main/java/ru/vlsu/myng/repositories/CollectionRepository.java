package ru.vlsu.myng.repositories;

import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Компонент слоя доступа к данным для работы с сущностью Collection.<br>
 * <br>
 * Обеспечивает операции сохранения, удаления и поиска пользовательских
 * коллекций.<br>
 * Используется в следующих сценариях:<br>
 * - создание новой коллекции пользователем;<br>
 * - получение всех коллекций конкретного пользователя;<br>
 * - редактирование названия коллекции;<br>
 * - удаление коллекции.<br>
 * <br>
 * Наследует стандартные CRUD-операции из JpaRepository.
 */
public interface CollectionRepository extends JpaRepository<Collection, Integer> {

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
     * @throws IllegalArgumentException                    если user равен null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    List<Collection> findByUser(User user);

    /**
     * Возвращает список коллекций пользователя, в которых отсутствует указанная игра.
     *
     * @param userId идентификатор пользователя — владельца коллекций.
     *               Не должен быть null.
     *               Должен соответствовать существующему пользователю.
     *
     * @param gameId идентификатор игры, которая не должна присутствовать
     *               в коллекциях.
     *               Не должен быть null.
     *               Должен соответствовать существующей игре.
     *
     * @return список коллекций пользователя, не содержащих указанную игру.
     *         Никогда не возвращает null.
     *         Может возвращать пустой список, если все коллекции уже
     *         содержат игру или коллекции отсутствуют.
     *
     * @throws IllegalArgumentException                    если userId или gameId равны null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    @Query("SELECT c FROM Collection c WHERE c.user.id = :userId AND c.id NOT IN " +
            "(SELECT col.id FROM Collection col JOIN col.games g WHERE g.id = :gameId)")
    List<Collection> findAllByUserIdAndGameNotPresent(@Param("userId") Integer userId,
                                                      @Param("gameId") Integer gameId);


    /**
     * Проверяет наличие коллекций у указанного пользователя.
     *
     * @param userId идентификатор пользователя.
     *               Не должен быть null.
     *               Должен соответствовать существующему пользователю.
     *
     * @return true, если у пользователя существует хотя бы одна коллекция;
     *         false — если коллекции отсутствуют.
     *
     * @throws IllegalArgumentException                    если userId равен null
     * @throws org.springframework.dao.DataAccessException
     *                                                     при ошибке доступа к базе
     *                                                     данных
     */
    boolean existsByUserId(Integer userId);
}