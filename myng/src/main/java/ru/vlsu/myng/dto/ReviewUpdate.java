package main.java.ru.vlsu.myng.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReviewUpdate {
    private String text;
    private Byte rating;
}