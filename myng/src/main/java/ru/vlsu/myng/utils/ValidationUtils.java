package ru.vlsu.myng.utils;

import org.springframework.web.multipart.MultipartFile;
import ru.vlsu.myng.dto.PublishGameRequest;

import java.util.*;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern GITHUB_PATTERN = Pattern.compile("^https://github\\.com/[\\w.-]+/[\\w.-]+/?$");
    private static final Pattern VERSION_PATTERN = Pattern.compile("^v\\d+(\\.\\d+)+$");
    private static final Pattern HASH_PATTERN = Pattern.compile("^[0-9a-fA-F]{7}$");
    private static final Pattern TAGS_PATTERN = Pattern.compile("^(#[a-z0-9-]+)(,\\s+#[a-z0-9-]+)*$");

    public static Map<String, String> validatePublishRequest(PublishGameRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Title validation
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            errors.put("title", "Название игры обязательно");
        } else if (request.getTitle().length() > 100) {
            errors.put("title", "Название не должно превышать 100 символов");
        }

        // Description validation
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            errors.put("description", "Описание обязательно");
        } else if (request.getDescription().length() > 2000) {
            errors.put("description", "Описание не должно превышать 2000 символов");
        }

        // Genre validation
        if (request.getGenre() == null || request.getGenre().trim().isEmpty()) {
            errors.put("genre", "Жанр обязателен");
        }

        // GitHub repo validation
        if (request.getRepoLink() == null || request.getRepoLink().trim().isEmpty()) {
            errors.put("repo_link", "Ссылка на репозиторий обязательна");
        } else if (!GITHUB_PATTERN.matcher(request.getRepoLink()).matches()) {
            errors.put("repo_link", "Формат: https://github.com/[username]/[repo-name]");
        }

        // Commit hash validation
        if (request.getCommitHash() == null || request.getCommitHash().trim().isEmpty()) {
            errors.put("commit_hash", "Хэш коммита обязателен");
        } else if (!HASH_PATTERN.matcher(request.getCommitHash()).matches()) {
            errors.put("commit_hash", "Должен быть 7 символов в шестнадцатеричном формате");
        }

        // Files validation
        if (request.getFiles() == null || request.getFiles().trim().isEmpty()) {
            errors.put("files", "Список файлов обязателен");
        }

        // Version validation
        if (request.getGameVer() == null || request.getGameVer().trim().isEmpty()) {
            errors.put("game_ver", "Версия игры обязательна");
        } else if (!VERSION_PATTERN.matcher(request.getGameVer()).matches()) {
            errors.put("game_ver", "Формат: v1.2.3");
        }

        // Tags validation (optional)
        if (request.getTags() != null && !request.getTags().trim().isEmpty()
                && !TAGS_PATTERN.matcher(request.getTags().trim()).matches()) {
            errors.put("tags", "Формат: #tag1, #tag2, #tag3");
        }

        // File validation
        if (request.getMainPic() == null || request.getMainPic().isEmpty()) {
            errors.put("main_pic", "Изображение обязательно");
        } else if (request.getMainPic().getSize() > 32 * 1024 * 1024) {
            errors.put("main_pic", "Размер файла не должен превышать 32MB");
        } else if (!request.getMainPic().getContentType().startsWith("image/")) {
            errors.put("main_pic", "Файл должен быть изображением");
        }

        return errors;
    }
}