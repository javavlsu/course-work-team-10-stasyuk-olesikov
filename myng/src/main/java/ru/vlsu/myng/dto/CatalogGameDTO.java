package ru.vlsu.myng.dto;

import lombok.Builder;
import lombok.Data;
import ru.vlsu.myng.entities.Game;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class CatalogGameDTO {
    private Integer id;
    private String name;
    private String description;
    private Game.Genre genre;
    private String developerUsername;
    private Set<String> tags;
    private Double averageRating;
    private Integer reviewsCount;
    private Integer totalViews;
    private Integer totalLaunches;
    private Instant firstReleaseDate;
    private String themeColor;
}