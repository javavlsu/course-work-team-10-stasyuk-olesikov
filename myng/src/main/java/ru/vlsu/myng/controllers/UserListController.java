package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.repositories.UserRepository;
import ru.vlsu.myng.entities.User;

@Controller
@RequiredArgsConstructor
public class UserListController {

    private final UserRepository userRepository;
    private final BanRepository banRepository;

    @GetMapping("/user-list")
    public String userListPage(Model model) {
        List<User> users = userRepository.findAll();

        Map<Integer, Boolean> bannedMap = new HashMap<>();
        Instant now = Instant.now();

        for (User user : users) {
            boolean isBanned = banRepository.existsByUser_IdAndStartTimeBeforeAndEndTimeAfter(
                    user.getId(), now, now);
            bannedMap.put(user.getId(), isBanned);
        }

        model.addAttribute("users", users);
        model.addAttribute("bannedMap", bannedMap);

        return "user_list";
    }
}