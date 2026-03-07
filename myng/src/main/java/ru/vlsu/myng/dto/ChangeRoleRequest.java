package ru.vlsu.myng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.vlsu.myng.entities.User;

/**
 * DTO для запроса на смену роли пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {
    private Integer userId;
    private User.Role newRole; // user, dev, mod, admin
}