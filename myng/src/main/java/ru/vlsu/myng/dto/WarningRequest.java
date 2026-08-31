package ru.vlsu.myng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для запроса на выдачу предупреждения пользователю.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningRequest {
    private Integer userId;
    private String reason;
}