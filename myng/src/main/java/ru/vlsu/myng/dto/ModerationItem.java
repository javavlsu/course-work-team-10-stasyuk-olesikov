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
    private String type;         // GAME_VERSION, DEV_APPLICATION, REVIEW
    private Integer gameId;         // Для GameVersion и Review

    // --- Для DEV_APPLICATION ---
    private String username;
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
    
    public ModerationItem() {}
}