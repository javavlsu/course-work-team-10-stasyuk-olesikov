package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.time.Instant;
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
}