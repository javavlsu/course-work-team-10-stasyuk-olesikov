package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ru.vlsu.myng.entities.Ban;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы со списком пользователей.
 * Содержит бизнес-логику получения пользователей и их статусов блокировки.
 */
@Service
@RequiredArgsConstructor
public class UserListService {

    private final UserRepository userRepository;
    private final BanRepository banRepository;

    /**
     * Получает список всех пользователей.
     *
     * @return список всех пользователей
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Получает информацию о блокировках для списка пользователей.
     *
     * @param users список пользователей
     * @return Map, где ключ - ID пользователя, значение - true если заблокирован
     */
    public Map<Integer, Boolean> getBannedStatusForUsers(List<User> users) {
        Map<Integer, Boolean> bannedMap = new HashMap<>();
        Instant now = Instant.now();

        for (User user : users) {
            boolean isBanned = banRepository.existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
                    user.getId(), now, now);
            bannedMap.put(user.getId(), isBanned);
        }

        return bannedMap;
    }

    /**
     * Получает список всех пользователей с их статусами блокировки.
     *
     * @return объект, содержащий список пользователей и Map со статусами блокировки
     */
    public UserListData getUserListWithBannedStatus() {
        List<User> users = getAllUsers();
        Map<Integer, Boolean> bannedMap = getBannedStatusForUsers(users);
        return new UserListData(users, bannedMap);
    }

    /**
     * Внутренний класс для передачи данных о пользователях и их статусах.
     */
    @lombok.Value
    public static class UserListData {
        List<User> users;
        Map<Integer, Boolean> bannedMap;
    }

    /**
     * Блокировка пользователя.
     *
     * @param userId        ID пользователя для блокировки
     * @param reason        причина блокировки
     * @param durationHours длительность блокировки в часах (null если навсегда)
     * @param moderator     модератор/админ, выполняющий блокировку
     * @throws RuntimeException если пользователь не найден или уже заблокирован
     */
    /**
     * Блокировка пользователя.
     */
    @Transactional
    public void banUser(Integer userId, String reason, Integer durationHours, User moderator) {
        // 1. Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Проверяем, не заблокирован ли уже
        boolean alreadyBanned = banRepository.existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
                userId, Instant.now(), Instant.now());
        if (alreadyBanned) {
            throw new RuntimeException("Пользователь уже заблокирован");
        }

        // 3. Создаем бан
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setModerator(moderator);
        ban.setReason(reason);
        ban.setStartTime(Instant.now());

        // 4. Устанавливаем время окончания
        if (durationHours == null || durationHours <= 0) {
            // Бессрочная блокировка - используем максимально допустимую дату для MySQL
            // timestamp
            // 2038-01-19 03:14:07 UTC
            ban.setEndTime(Instant.parse("2038-01-19T03:14:07Z"));
        } else {
            ban.setEndTime(Instant.now().plus(durationHours, ChronoUnit.HOURS));

            // Проверяем, не вышли ли за пределы 2038 года
            Instant maxDate = Instant.parse("2038-01-19T03:14:07Z");
            if (ban.getEndTime().isAfter(maxDate)) {
                ban.setEndTime(maxDate); // ограничиваем максимальной датой
            }
        }

        // 5. Сохраняем
        banRepository.save(ban);
    }

    /**
     * Разблокировка пользователя.
     *
     * @param userId ID пользователя для разблокировки
     * @throws RuntimeException если пользователь не найден или не заблокирован
     */
    @Transactional
    public void unbanUser(Integer userId) {
        // 1. Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Ищем активный бан
        // Для простоты будем искать все баны пользователя и деактивировать последний
        // В реальном проекте лучше добавить метод в репозиторий для поиска активного
        // бана
        Instant now = Instant.now();
        boolean hasActiveBan = banRepository.existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
                userId, now, now);

        if (!hasActiveBan) {
            throw new RuntimeException("У пользователя нет активной блокировки");
        }

        // Получаем все баны пользователя
        List<Ban> userBans = banRepository.findByUser(user);

        // Находим активный бан (где endTime > now) и устанавливаем endTime = now
        userBans.stream()
                .filter(ban -> ban.getEndTime().isAfter(now))
                .findFirst()
                .ifPresent(ban -> {
                    ban.setEndTime(now); // завершаем бан сейчас
                    banRepository.save(ban);
                });
    }

    /**
     * Смена роли пользователя.
     *
     * @param userId  ID пользователя
     * @param newRole новая роль
     * @param admin   администратор, выполняющий смену роли
     * @throws RuntimeException если пользователь не найден или недостаточно прав
     */
    @Transactional
    public void changeUserRole(Integer userId, User.Role newRole, User admin) {
        // 1. Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Проверяем, что админ не меняет сам себе роль (опционально)
        if (user.getId().equals(admin.getId())) {
            throw new RuntimeException("Нельзя изменить свою собственную роль");
        }

        // 3. Проверяем, что роль действительно меняется
        if (user.getRole() == newRole) {
            throw new RuntimeException("У пользователя уже эта роль");
        }

        // 4. Сохраняем старую роль для уведомления
        User.Role oldRole = user.getRole();

        // 5. Меняем роль
        user.setRole(newRole);
        userRepository.save(user);

        // 6. Создаем уведомление для пользователя об изменении роли (опционально)
        // TODO: добавить создание уведомления
    }
}