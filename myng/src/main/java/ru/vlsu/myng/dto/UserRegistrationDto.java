package ru.vlsu.myng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) для передачи данных регистрации пользователя.
 * <p>
 * Используется для приема данных из формы регистрации, чтобы отделить
 * входящие данные от сущности {@link ru.vlsu.myng.entities.User}.
 * Это повышает безопасность, так как мы контролируем, какие поля могут
 * быть установлены через запрос.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    /**
     * Имя пользователя (уникальное).
     * Должно соответствовать ограничениям из сущности User:
     * - не null
     * - максимум 20 символов
     */
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(max = 20, message = "Имя пользователя не может быть длиннее 20 символов")
    private String username;

    /**
     * Email пользователя (уникальный).
     */
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    /**
     * Пароль пользователя.
     * Минимальная длина - 6 символов.
     * Не хранится в БД в открытом виде, а хешируется через BCrypt.
     */
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String password;

    /**
     * Подтверждение пароля.
     * Используется только для проверки, что пользователь не ошибся при вводе.
     * Не сохраняется в БД.
     */
    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;

    /**
     * Проверяет, совпадают ли пароль и подтверждение пароля.
     *
     * @return true если пароли совпадают и не равны null
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Проверяет, валидны ли основные поля.
     * Упрощенная валидация на уровне DTO.
     *
     * @return true если все поля заполнены корректно
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty()
                && username.length() <= 20
                && email != null && !email.trim().isEmpty()
                && password != null && password.length() >= 6
                && isPasswordConfirmed();
    }
}