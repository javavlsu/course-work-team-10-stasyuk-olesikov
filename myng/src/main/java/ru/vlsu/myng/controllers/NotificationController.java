package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.myng.services.NotificationService;
import ru.vlsu.myng.entities.User;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ru.vlsu.myng.services.UserService userService;

    @GetMapping("/user/{userId}")
    public String getUserNotifications(@PathVariable Integer userId, Model model) {
        model.addAttribute("notifications", notificationService.getByUser(userId));
        return "fragments/notifications :: notificationsFragment";
    }

    @PostMapping("/delete/{notificationId}")
    @ResponseBody
    public void deleteNotification(@PathVariable Integer notificationId,
                                   Principal principal) {
        User user = userService.findByEmail(principal.getName());

        notificationService.removeNotificationForUser(notificationId, user.getId());
    }
}