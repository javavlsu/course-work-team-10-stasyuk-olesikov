package ru.vlsu.myng.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface MaxFileSize
{
    String message() default "Файл слишком большой";
    long value();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
