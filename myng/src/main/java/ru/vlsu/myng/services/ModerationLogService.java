package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationLogService {

    private final ModerationVerdictRepository verdictRepository;

    public List<ModerationItem> getModerationItems() {
        List<ModerationVerdict> verdicts = verdictRepository.findAll();

        return verdicts.stream()
                .map(this::toModerationItem)
                .sorted(Comparator.comparing(ModerationItem::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Page<ModerationItem> getModerationItems(
            String search,
            String type,
            String status,
            String period,
            Pageable pageable ) {

        Instant createdAfter = switch (
                period == null ? "" : period
                ) {

            case "today" ->
                    Instant.now().minus(1, ChronoUnit.DAYS);

            case "week" ->
                    Instant.now().minus(7, ChronoUnit.DAYS);

            case "month" ->
                    Instant.now().minus(30, ChronoUnit.DAYS);

            default -> null;
        };
        
        return verdictRepository.getModerationItems(search, type, status, createdAfter, pageable);
    }

    private ModerationItem toModerationItem(ModerationVerdict verdict) {
        ModerationItem dto = new ModerationItem();

        // --- MODERATOR ---
        if (verdict.getModerator() != null) {
            dto.setModeratorUsername(verdict.getModerator().getUsername());
        }
        dto.setApproved(verdict.getApproved());
        dto.setReason(verdict.getReason());

        // --- GAME VERSION ---
        if (verdict.getGameVersion() != null) {
            var version = verdict.getGameVersion();
            dto.setId(version.getId());
            dto.setType("GAME_VERSION");
            dto.setGameId(version.getGame().getId());
            dto.setCommitHash(version.getCommitHash());
            dto.setChangelog(version.getChangelog());
            dto.setRepoUrl(version.getGame().getRepo());
            dto.setCreatedAt(version.getCreatedAt());
        }
        // --- DEV APPLICATION ---
        else if (verdict.getDevApplication() != null) {
            var app = verdict.getDevApplication();
            dto.setId(app.getId());
            dto.setType("DEV_APPLICATION");
            dto.setUsername(app.getUser().getUsername());
            dto.setGithubLogin(app.getGithubUsername());
            dto.setDescription(app.getText());
            dto.setCreatedAt(app.getCreatedAt());
        }
        // --- REVIEW ---
        else if (verdict.getReview() != null) {
            var review = verdict.getReview();
            dto.setId(review.getId());
            dto.setType("REVIEW");
            dto.setGameId(review.getGame().getId());
            dto.setRating(review.getRating() != null ? review.getRating().intValue() : null);
            dto.setReviewText(review.getText());
            dto.setReportCount(review.getReportCount());
            dto.setCreatedAt(review.getCreatedAt());
        }

        return dto;
    }
}