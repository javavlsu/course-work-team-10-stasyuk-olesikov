package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vlsu.myng.dto.BanRequest;
import ru.vlsu.myng.dto.ChangeRoleRequest;
import ru.vlsu.myng.dto.UnbanRequest;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.services.UserListService;
import ru.vlsu.myng.services.UserService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserListController {

    private final UserListService userListService;
    private final UserService userService; // нужен для поиска модератора

    @GetMapping("/user-list")
    public String userListPage(Model model) {
        UserListService.UserListData data = userListService.getUserListWithBannedStatus();

        model.addAttribute("users", data.getUsers());
        model.addAttribute("bannedMap", data.getBannedMap());

        return "user_list";
    }

    /**
     * Блокировка пользователя (AJAX запрос).
     * Возвращает JSON с результатом.
     */
    @PostMapping("/user-list/ban")
    @ResponseBody
    public Map<String, Object> banUser(@RequestBody BanRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Находим текущего модератора/админа
            User moderator = userService.findByEmail(currentUser.getUsername());

            // Выполняем блокировку
            userListService.banUser(
                    request.getUserId(),
                    request.getReason(),
                    request.isPermanent() ? null : request.getDurationHours(),
                    moderator);

            response.put("success", true);
            response.put("message", "Пользователь заблокирован");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Разблокировка пользователя (AJAX запрос).
     */
    @PostMapping("/user-list/unban")
    @ResponseBody
    public Map<String, Object> unbanUser(@RequestBody UnbanRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            userListService.unbanUser(request.getUserId());
            response.put("success", true);
            response.put("message", "Пользователь разблокирован");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Смена роли пользователя (AJAX запрос).
     */
    @PostMapping("/user-list/change-role")
    @ResponseBody
    public Map<String, Object> changeUserRole(@RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Находим текущего админа
            User admin = userService.findByEmail(currentUser.getUsername());

            // Выполняем смену роли
            userListService.changeUserRole(request.getUserId(), request.getNewRole(), admin);

            response.put("success", true);
            response.put("message", "Роль пользователя успешно изменена");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}