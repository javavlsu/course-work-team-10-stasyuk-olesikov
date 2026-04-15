package ru.vlsu.myng.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.vlsu.myng.repositories.GameRepository;

public class UniqueRepoValidator implements ConstraintValidator<UniqueRepo, String>
{

    private final GameRepository gameRepository;

    public UniqueRepoValidator(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank will handle null/empty
        }

        return !gameRepository.existsByRepo(value);
    }
}