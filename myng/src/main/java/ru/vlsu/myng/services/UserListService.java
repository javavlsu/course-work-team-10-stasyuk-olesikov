package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ru.vlsu.myng.entities.Ban;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.Warning;
import ru.vlsu.myng.entities.Notification;
import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.repositories.NotificationRepository;
import ru.vlsu.myng.repositories.UserRepository;
import ru.vlsu.myng.repositories.WarningRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
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
    private final NotificationRepository notificationRepository;
    private final WarningRepository warningRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean alreadyBanned = banRepository.existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
                userId, Instant.now(), Instant.now());
        if (alreadyBanned) {
            throw new RuntimeException("Пользователь уже заблокирован");
        }

        Ban ban = new Ban();
        ban.setUser(user);
        ban.setModerator(moderator);
        ban.setReason(reason);
        ban.setStartTime(Instant.now());

        if (durationHours == null || durationHours <= 0) {
            // ВОЗМОЖНО ПОТОМ ПОМЕНЯТЬ В БД!
            // Бессрочная блокировка - используем максимально допустимую дату для MySQL
            // timestamp
            // 2038-01-19 03:14:07 UTC
            ban.setEndTime(Instant.parse("2038-01-19T03:14:07Z"));
        } else {
            ban.setEndTime(Instant.now().plus(durationHours, ChronoUnit.HOURS));

            Instant maxDate = Instant.parse("2038-01-19T03:14:07Z");
            if (ban.getEndTime().isAfter(maxDate)) {
                ban.setEndTime(maxDate);
            }
        }

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Instant now = Instant.now();
        boolean hasActiveBan = banRepository.existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
                userId, now, now);

        if (!hasActiveBan) {
            throw new RuntimeException("У пользователя нет активной блокировки");
        }

        List<Ban> userBans = banRepository.findByUser(user);

        userBans.stream()
                .filter(ban -> ban.getEndTime().isAfter(now))
                .findFirst()
                .ifPresent(ban -> {
                    ban.setEndTime(now);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user.getId().equals(admin.getId())) {
            throw new RuntimeException("Нельзя изменить свою собственную роль");
        }

        if (user.getRole() == newRole) {
            throw new RuntimeException("У пользователя уже эта роль");
        }

        User.Role oldRole = user.getRole();

        user.setRole(newRole);
        userRepository.save(user);

        createRoleChangeNotification(user, oldRole, newRole, admin);
    }

    /**
     * Создает уведомление о смене роли.
     *
     * @param user    пользователь, которому меняют роль
     * @param oldRole старая роль
     * @param newRole новая роль
     * @param admin   администратор, выполнивший изменение
     */
    private void createRoleChangeNotification(User user, User.Role oldRole, User.Role newRole, User admin) {
        String roleText = switch (newRole) {
            case user -> "пользователя";
            case dev -> "разработчика";
            case mod -> "модератора";
            case admin -> "администратора";
        };

        String notificationText = String.format(
                "Ваша роль изменена с '%s' на '%s' (изменено администратором @%s)",
                getRoleDisplayName(oldRole),
                getRoleDisplayName(newRole),
                admin.getUsername());

        Notification notification = new Notification();
        notification.setCreatedAt(Instant.now());
        notification.setType(Notification.Type.system);
        notification.setText(notificationText);

        notification.setUsers(new HashSet<>());
        notification.getUsers().add(user);

        notificationRepository.save(notification);
    }

    /**
     * Возвращает отображаемое имя роли.
     */
    private String getRoleDisplayName(User.Role role) {
        return switch (role) {
            case user -> "пользователь";
            case dev -> "разработчик";
            case mod -> "модератор";
            case admin -> "администратор";
        };
    }

    /**
     * Выдача предупреждения пользователю.
     *
     * @param userId    ID пользователя
     * @param reason    причина предупреждения
     * @param moderator модератор/админ, выдающий предупреждение
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public void issueWarning(Integer userId, String reason, User moderator) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Причина предупреждения не может быть пустой");
        }

        Warning warning = new Warning();
        warning.setUser(user);
        warning.setModerator(moderator);
        warning.setReason(reason);

        warningRepository.save(warning);

        createWarningNotification(user, reason, moderator);
    }

    /**
     * Создает уведомление о предупреждении.
     *
     * @param user      пользователь, получивший предупреждение
     * @param reason    причина предупреждения
     * @param moderator модератор, выдавший предупреждение
     */
    private void createWarningNotification(User user, String reason, User moderator) {
        String notificationText = String.format(
                "Вам вынесено предупреждение от модератора @%s: %s",
                moderator.getUsername(),
                reason);

        Notification notification = new Notification();
        notification.setCreatedAt(Instant.now());
        notification.setType(Notification.Type.warning);
        notification.setText(notificationText);

        notification.setUsers(new HashSet<>());
        notification.getUsers().add(user);

        notificationRepository.save(notification);
    }
}