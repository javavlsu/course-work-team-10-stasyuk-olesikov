package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.DevApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для работы с заявками на получение роли разработчика.<br>
 * <br>
 * Обеспечивает бизнес-логику для управления заявками:<br>
 * - сохранение новой заявки от пользователя;<br>
 * - поиск всех заявок, поданных указанным пользователем.<br>
 * <br>
 * Используется в следующих сценариях:<br>
 * - пользователь подаёт заявку на становление разработчиком;<br>
 * - модератор просматривает список заявок конкретного пользователя;<br>
 * - администратор анализирует историю заявок пользователя.<br>
 * <br>
 * Является прослойкой между контроллерами и
 * {@link ru.vlsu.myng.repositories.DevApplicationRepository},
 * инкапсулируя прямые операции с базой данных.
 */
@Service
@RequiredArgsConstructor
public class DevApplicationService {
    private final DevApplicationRepository repo;

    /**
     * Сохраняет заявку на становление разработчиком в базе данных.
     *
     * <p>
     * Если у заявки уже есть id, она будет обновлена.
     * Если id равен null, будет создана новая запись.
     * </p>
     *
     * <p>
     * Ограничения целостности:
     * </p>
     * <ul>
     *   <li>один пользователь может иметь только одну активную заявку
     *       (ограничение на уровне БД — уникальный внешний ключ);</li>
     *   <li>все поля заявки, отмеченные как обязательные,
     *       должны быть заполнены.</li>
     * </ul>
     *
     * @param app заявка для сохранения. Не должна быть null.
     *
     * @return сохранённая заявка с присвоенным id
     *         (если создавалась новая).
     *
     * @throws org.springframework.dao.DataIntegrityViolationException
     *         если:
     *         <ul>
     *           <li>пользователь уже имеет заявку на рассмотрении;</li>
     *           <li>нарушены другие ограничения целостности.</li>
     *         </ul>
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    public DevApplication save(DevApplication app) {
        return repo.save(app);
    }

    /**
     * Возвращает список всех заявок, поданных указанным пользователем.
     *
     * <p>
     * Пользователь может подавать несколько заявок за всю историю,
     * если предыдущие были отклонены или отозваны.
     * </p>
     *
     * @param u пользователь, чьи заявки требуется получить.
     *          Не должен быть null.
     *
     * @return список заявок пользователя. Никогда не возвращает null.
     *         Может быть пустым, если пользователь ещё не подавал заявок.
     *
     * @throws IllegalArgumentException если пользователь равен null
     * @throws org.springframework.dao.DataAccessException
     *         при ошибке доступа к базе данных
     */
    public List<DevApplication> findByUser(User u) {
        return repo.findByUser(u);
    }
}