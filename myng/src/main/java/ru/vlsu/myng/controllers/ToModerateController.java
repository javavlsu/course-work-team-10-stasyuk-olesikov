package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.services.ModerationVerdictService;
import ru.vlsu.myng.services.ToModerateService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/to-moderate")
public class ToModerateController {

    private final ToModerateService toModerateService;
    private final ModerationVerdictService moderationVerdictService;

    @GetMapping("")
    public String toModeratePage(Model model) {
        List<ModerationItem> items = toModerateService.getPendingModerationItems();
        model.addAttribute("items", items);
        return "to_moderate";
    }

    @PostMapping("/approve/{moderationVerdictId}")
    public ResponseEntity<Void> approve(
            @PathVariable Integer moderationVerdictId,
            @AuthenticationPrincipal User user
    ) {

        moderationVerdictService.approve(moderationVerdictId, user);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{moderationVerdictId}")
    public ResponseEntity<Void> reject(
            @PathVariable Integer moderationVerdictId,
            @RequestParam String reason,
            @AuthenticationPrincipal User user
    ) {
        moderationVerdictService.reject(moderationVerdictId, reason, user);

        return ResponseEntity.ok().build();
    }
}