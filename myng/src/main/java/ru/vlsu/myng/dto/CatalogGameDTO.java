package ru.vlsu.myng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.vlsu.myng.entities.Game;

import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class CatalogGameDTO {
    private Integer id;
    private String name;
    private String description;
    private Game.Genre genre;
    private String developerUsername;
    private Set<String> tags;
    private String base64Image;
    private Double averageRating;
    private Integer reviewsCount;
    private Integer totalViews;
    private Integer totalLaunches;
    private Instant firstReleaseDate;
    private String themeColor;

    public CatalogGameDTO(
            Integer id,
            String name,
            String description,
            Game.Genre genre,
            String developerUsername,
            Double averageRating,
            Long reviewsCount,
            Long totalViews,
            Long totalLaunches,
            Instant firstReleaseDate,
            byte[] image
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.developerUsername = developerUsername;

        this.averageRating =
                averageRating != null ? averageRating : 0.0;

        this.reviewsCount =
                reviewsCount != null ? reviewsCount.intValue() : 0;

        this.totalViews =
                totalViews != null ? totalViews.intValue() : 0;

        this.totalLaunches =
                totalLaunches != null ? totalLaunches.intValue() : 0;

        this.firstReleaseDate = firstReleaseDate;

        if (image != null) {
            this.base64Image =
                    "data:image/jpeg;base64," +
                            Base64.getEncoder().encodeToString(image);
        }
    }
}