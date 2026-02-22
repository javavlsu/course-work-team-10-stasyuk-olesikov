package ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ToModerateController {

    private final ModerationVerdictRepository verdictRepository;

    public ToModerateController(ModerationVerdictRepository verdictRepository) {
        this.verdictRepository = verdictRepository;
    }

    @GetMapping("/to-moderate")
    public String toModeratePage(Model model) {

        List<ModerationItem> items = new ArrayList<>();

        // --- Game Versions pending moderation ---
        verdictRepository.findByGameVersionIsNotNullAndApprovedIsNull()
                .forEach(v -> {
                    ModerationItem item = new ModerationItem();
                    item.setId(v.getGameVersion().getId().longValue());
                    item.setType("GAME_VERSION");
                    item.setGameId(v.getGameVersion().getGame().getId().longValue());
                    item.setCommitHash(v.getGameVersion().getCommitHash());
                    item.setChangelog(v.getGameVersion().getChangelog());
                    item.setRepoUrl(v.getGameVersion().getGame().getRepo());
                    item.setCreatedAt(v.getGameVersion().getCreatedAt());
                    items.add(item);
                });

        // --- Developer Applications pending moderation ---
        verdictRepository.findByDevApplicationIsNotNullAndApprovedIsNull()
                .forEach(v -> {
                    ModerationItem item = new ModerationItem();
                    item.setId(v.getDevApplication().getId().longValue());
                    item.setType("DEV_APPLICATION");
                    item.setUsername(v.getDevApplication().getUser().getUsername());
                    item.setGithubLogin(v.getDevApplication().getGithubUsername());
                    item.setDescription(v.getDevApplication().getText());
                    item.setCreatedAt(v.getDevApplication().getCreatedAt());
                    items.add(item);
                });

        // --- Reviews pending moderation ---
        verdictRepository.findByReviewIsNotNullAndApprovedIsNull()
                .forEach(v -> {
                    ModerationItem item = new ModerationItem();
                    item.setId(v.getReview().getId().longValue());
                    item.setType("REVIEW");
                    item.setGameId(v.getReview().getGame().getId().longValue());
                    item.setRating(v.getReview().getRating() != null ? v.getReview().getRating().intValue() : null);
                    item.setReviewText(v.getReview().getText());
                    item.setReportCount(v.getReview().getReportCount());
                    item.setCreatedAt(v.getReview().getCreatedAt());
                    items.add(item);
                });

        model.addAttribute("items", items);
        return "to_moderate";
    }
}