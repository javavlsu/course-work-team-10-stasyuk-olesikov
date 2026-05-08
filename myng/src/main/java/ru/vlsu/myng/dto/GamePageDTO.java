package ru.vlsu.myng.dto;

import lombok.Builder;
import lombok.Data;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.Tag;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GamePageDTO {
    private Integer id;
    private String name;
    private String description;
    private String themeColor;
    private Game.Genre genre;
    private User developer;
    private Set<Tag> tags;
    private String base64Image;

    private Double averageRating;
    private Integer totalViews;
    private Integer totalLaunches;
    private Integer reviewsCount;

    private List<GameVersion> versions;
    private GameVersion latestVersion;

    private List<Review> recentReviews;

    private Instant firstReleaseDate;
    private Instant lastUpdateDate;
}