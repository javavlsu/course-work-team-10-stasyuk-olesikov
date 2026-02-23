package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ToModerateService {

    private final ModerationVerdictRepository verdictRepository;

    public List<ModerationItem> getPendingModerationItems() {
        List<ModerationItem> items = new ArrayList<>();

        // --- Game Versions pending moderation ---
        verdictRepository.findByGameVersionIsNotNullAndApprovedIsNull()
                .forEach(v -> {
                    var version = v.getGameVersion();
                    ModerationItem item = new ModerationItem();
                    item.setId(version.getId().longValue());
                    item.setType("GAME_VERSION");
                    item.setGameId(version.getGame().getId().longValue());
                    item.setCommitHash(version.getCommitHash());
                    item.setChangelog(version.getChangelog());
                    item.setRepoUrl(version.getGame().getRepo());
                    item.setCreatedAt(version.getCreatedAt());
                    items.add(item);
                });

        // --- Developer Applications pending moderation ---
        verdictRepository.findByDevApplicationIsNotNullAndApprovedIsNull()
                .forEach(v -> {
                    var app = v.getDevApplication();
                    ModerationItem item = new ModerationItem();
                    item.setId(app.getId().longValue());
                    item.setType("DEV_APPLICATION");
                    item.setUsername(app.getUser().getUsername());
                    item.setGithubLogin(app.getGithubUsername());
                    item.setDescription(app.getText());
                    item.setCreatedAt(app.getCreatedAt());
                    items.add(item);
                });

        // --- Reviews pending moderation ---
        verdictRepository.findByReviewIsNotNullAndApprovedIsNull()
                .forEach(v -> {
                    var review = v.getReview();
                    ModerationItem item = new ModerationItem();
                    item.setId(review.getId().longValue());
                    item.setType("REVIEW");
                    item.setGameId(review.getGame().getId().longValue());
                    item.setRating(review.getRating() != null ? review.getRating().intValue() : null);
                    item.setReviewText(review.getText());
                    item.setReportCount(review.getReportCount());
                    item.setCreatedAt(review.getCreatedAt());
                    items.add(item);
                });

        return items;
    }
}