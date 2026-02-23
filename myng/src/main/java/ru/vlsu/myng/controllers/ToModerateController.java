package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.services.ToModerateService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ToModerateController {

    private final ToModerateService toModerateService;

    @GetMapping("/to-moderate")
    public String toModeratePage(Model model) {
        List<ModerationItem> items = toModerateService.getPendingModerationItems();
        model.addAttribute("items", items);
        return "to_moderate";
    }
}