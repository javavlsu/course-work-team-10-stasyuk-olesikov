package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;
import ru.vlsu.myng.services.ModerationLogService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ModerationLogController {

    private final ModerationLogService moderationLogService;

    @GetMapping("/moderation-log")
    public String moderationPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        Page<ModerationItem> moderationPage =
                moderationLogService.getModerationItems(pageable);

        System.out.println("moderationLog pageable content: " + moderationPage.getContent());

        model.addAttribute("moderationPage", moderationPage);
        model.addAttribute("moderationItems", moderationPage.getContent());

        return "moderation_log";
    }
}