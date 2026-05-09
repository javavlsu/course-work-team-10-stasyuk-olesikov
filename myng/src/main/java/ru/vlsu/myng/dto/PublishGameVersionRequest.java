package ru.vlsu.myng.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PublishGameVersionRequest {
    @NotNull
    private Integer gameId;

    @NotBlank(message = "Версия обязательна")
    @Pattern(
            regexp = "^v\\d+\\.\\d+\\.\\d+$",
            message = "Версия должна быть в формате v1.0.0"
    )
    private String gameVerName;

    @NotBlank(message = "Хэш коммита обязателен")
    @Pattern(
            regexp = "^[a-fA-F0-9]{7}$",
            message = "Хэш должен быть 7-значным HEX"
    )
    private String commitHash;

    @Pattern(
            regexp = "^([a-zA-Z0-9_\\-./]+)(,\\s*[a-zA-Z0-9_\\-./]+)*$",
            message = "Введите названия файлов и папок в формате: file.ext, folder, dir/file.ext"
    )
    private String files;

    @Length(min = 10, max = 500, message = "Список изменений должен быть от 10 до 500 символов")
    private String changelog;
}