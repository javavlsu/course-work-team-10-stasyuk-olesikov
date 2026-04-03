package ru.vlsu.myng.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MyGame {
    private Integer id;
    private String name;
    private String descr;
    private boolean approved; // "Опубликована" or "На модерации"
    private Integer viewCount;
    private Double rating; // average rating
}