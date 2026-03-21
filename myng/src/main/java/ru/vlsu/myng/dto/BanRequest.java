package ru.vlsu.myng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для запроса на блокировку пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanRequest {
    private Integer userId;
    private String reason;
    private Integer durationHours;
    
    private boolean permanent;
}