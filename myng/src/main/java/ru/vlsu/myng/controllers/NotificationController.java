package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.myng.services.NotificationService;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public String getUserNotifications(@PathVariable Integer userId, Model model) {
        model.addAttribute("notifications", notificationService.getByUser(userId));
        return "fragments/notifications :: notificationsFragment";
    }
}