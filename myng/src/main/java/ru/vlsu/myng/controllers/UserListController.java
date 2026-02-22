package  ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserListController {

    @GetMapping("/user-list")
    public String userListPage() {
        return "user_list";
    }
}