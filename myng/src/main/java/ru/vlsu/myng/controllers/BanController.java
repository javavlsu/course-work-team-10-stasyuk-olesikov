package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.myng.entities.Ban;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.BanRepository;
import ru.vlsu.myng.services.UserService;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class BanController {

    private final BanRepository banRepository;
    private final UserService userService;

    @GetMapping("/banned")
    public String banned(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            Model model
    ) {

        if (user == null) {
            return "redirect:/";
        }

        User u = userService.findByEmail(user.getUsername());

        Ban activeBan = banRepository
                .findFirstByUserAndEndTimeAfterOrderByEndTimeDesc(
                        u,
                        Instant.now()
                )
                .orElse(null);


        if (activeBan == null) {
            return "redirect:/";
        }

        model.addAttribute("ban", activeBan);

        return "banned";
    }
}