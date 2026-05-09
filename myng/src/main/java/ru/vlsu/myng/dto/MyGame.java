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
    private Boolean approved;
    private String status; // "Опубликована", "На модерации" или "Отклонена"
    private Integer viewCount;
    private Double rating; // average rating
    private String image;
    private String bgClass;
}