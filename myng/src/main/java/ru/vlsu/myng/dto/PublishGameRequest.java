package ru.vlsu.myng.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PublishGameRequest {
    private String title;
    private String description;
    private String genre;
    private String repoLink;
    private String commitHash;
    private String files;
    private String tags;
    private String gameVer;
    private MultipartFile mainPic;
}