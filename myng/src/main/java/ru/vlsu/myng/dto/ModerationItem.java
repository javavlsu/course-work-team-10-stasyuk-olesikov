package ru.vlsu.myng.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class ModerationItem {

    private Integer moderationVerdictId;

    private Integer id;
    private String type; // GAME_VERSION, DEV_APPLICATION, REVIEW
    private Integer gameId; // Для GameVersion и Review

    // --- Для DEV_APPLICATION ---
    private String username;
    private String githubUrl;
    private String githubLogin;
    private String description;

    // --- Для GAME_VERSION ---
    private String commitHash;
    private String changelog;
    private String repoUrl;

    // --- Для REVIEW ---
    private Integer rating;
    private String reviewText;
    private Integer reportCount;

    private Instant createdAt;
    private String moderatorUsername;
    private Boolean approved;
    private String reason;

    public ModerationItem() {
    }

    // for getModerationItems projection query
    public ModerationItem(
            Integer moderationVerdictId,
            Integer id,
            String type,
            Integer gameId,
            String username,
            String githubLogin,
            String description,
            String commitHash,
            String changelog,
            String repoUrl,
            Integer rating,
            String reviewText,
            Integer reportCount,
            Instant createdAt,
            String moderatorUsername,
            Boolean approved,
            String reason
    ) {
        this.moderationVerdictId = moderationVerdictId;
        this.id = id;
        this.type = type;
        this.gameId = gameId;
        this.username = username;
        this.githubLogin = githubLogin;
        this.description = description;
        this.commitHash = commitHash;
        this.changelog = changelog;
        this.repoUrl = repoUrl;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reportCount = reportCount;
        this.createdAt = createdAt;
        this.moderatorUsername = moderatorUsername;
        this.approved = approved;
        this.reason = reason;
    }
}