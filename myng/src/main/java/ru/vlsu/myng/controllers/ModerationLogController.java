package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.services.ModerationLogService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ModerationLogController {

    private final ModerationLogService moderationLogService;

    @GetMapping("/moderation-log")
    public String moderationPage(Model model) {
        List<ModerationItem> items = moderationLogService.getModerationItems();
        model.addAttribute("moderationItems", items);
        return "moderation_log";
    }
}