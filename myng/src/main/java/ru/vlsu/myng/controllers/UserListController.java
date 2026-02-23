package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.myng.services.UserListService;

@Controller
@RequiredArgsConstructor
public class UserListController {

    private final UserListService userListService;

    @GetMapping("/user-list")
    public String userListPage(Model model) {
        UserListService.UserListData data = userListService.getUserListWithBannedStatus();

        model.addAttribute("users", data.getUsers());
        model.addAttribute("bannedMap", data.getBannedMap());

        return "user_list";
    }
}