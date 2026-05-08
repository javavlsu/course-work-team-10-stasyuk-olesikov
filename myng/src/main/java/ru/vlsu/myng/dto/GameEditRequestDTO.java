package ru.vlsu.myng.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class GameEditRequestDTO {
    private String name;
    private String description;
    private MultipartFile image;
}