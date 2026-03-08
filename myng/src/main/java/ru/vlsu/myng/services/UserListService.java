package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserListService {

    private final UserRepository userRepository;
    private final BanRepository banRepository;
    private final NotificationRepository notificationRepository;
    private final WarningRepository warningRepository;

    /**
     * Получает страницу пользователей с их статусами блокировки с учётом фильтров.
     *
     * @param pageable информация о пагинации
     * @param search   поисковый запрос
     * @param role     фильтр по роли
     * @param status   фильтр по статусу (active/blocked)
     * @return объект, содержащий страницу пользователей и Map со статусами
     *         блокировки
     */
    @Transactional(readOnly = true)
    public UserListData getUserListWithBannedStatus(Pageable pageable, String search, User.Role role, String status) {
        Page<User> usersPage = userRepository.findWithFilters(search, role, pageable);

        Page<User> resultPage;
        Map<Integer, Boolean> resultBannedMap;

        if (status != null && !status.isEmpty()) {
            Map<Integer, Boolean> currentBannedMap = getBannedStatusForUsers(usersPage.getContent());

            List<User> filteredUsers = usersPage.getContent().stream()
                    .filter(user -> {
                        boolean isBanned = currentBannedMap.getOrDefault(user.getId(), false);
                        return status.equals("blocked") ? isBanned : !isBanned;
                    })
                    .collect(Collectors.toList());

            resultPage = new PageImpl<>(
                    filteredUsers,
                    pageable,
                    filteredUsers.size());

            resultBannedMap = getBannedStatusForUsers(filteredUsers);
        } else {
            resultPage = usersPage;
            resultBannedMap = getBannedStatusForUsers(usersPage.getContent());
        }

        return new UserListData(resultPage, resultBannedMap);
    }

    /**
     * Получает информацию о блокировках для списка пользователей.
     */
    private Map<Integer, Boolean> getBannedStatusForUsers(List<User> users) {
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
     * Внутренний класс для передачи данных о пользователях и их статусах.
     */
    @lombok.Value
    public static class UserListData {
        Page<User> users;
        Map<Integer, Boolean> bannedMap;
    }

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
            // Бессрочная блокировка
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

    private void createRoleChangeNotification(User user, User.Role oldRole, User.Role newRole, User admin) {
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

    private String getRoleDisplayName(User.Role role) {
        return switch (role) {
            case user -> "пользователь";
            case dev -> "разработчик";
            case mod -> "модератор";
            case admin -> "администратор";
        };
    }

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