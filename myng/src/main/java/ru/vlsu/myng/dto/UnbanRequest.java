package ru.vlsu.myng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для запроса на разблокировку пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnbanRequest {
    private Integer userId;
}