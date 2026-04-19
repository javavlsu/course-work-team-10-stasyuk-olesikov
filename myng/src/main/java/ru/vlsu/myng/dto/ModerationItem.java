package ru.vlsu.myng.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ModerationItem {

    private Integer moderationVerdictId;

    private Long id;
    private String type;         // GAME_VERSION, DEV_APPLICATION, REVIEW
    private Long gameId;         // Для GameVersion и Review

    // --- Для DEV_APPLICATION ---
    private String username;
    private String githubLogin;
    private String description;
    private String githubUrl;

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
}