package ru.vlsu.myng.dto;

import lombok.Data;
import ru.vlsu.myng.entities.Game;
import java.util.Set;

@Data
public class GameFilterDTO {
    private String search;
    private Game.Genre genre;
    private Set<String> tags;
    private Double minRating;
    private String sort;
}