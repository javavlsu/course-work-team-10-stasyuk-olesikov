package ru.vlsu.myng.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileNotEmptyValidator
        implements ConstraintValidator<FileNotEmpty, MultipartFile>
{

    @Override
    public boolean isValid(MultipartFile file,
                           ConstraintValidatorContext context) {

        return file != null && !file.isEmpty();
    }
}