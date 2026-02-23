package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vlsu.myng.dto.UserRegistrationDto;
import ru.vlsu.myng.services.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/auth")
    public String authPage(Model model) {
        // Добавляем пустой объект для формы регистрации
        if (!model.containsAttribute("userRegistrationDto")) {
            model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        }
        return "auth";
    }

    @PostMapping("/auth/register")
    public String registerUser(
            @ModelAttribute UserRegistrationDto registrationDto,
            RedirectAttributes redirectAttributes) {

        try {
            // Пытаемся зарегистрировать пользователя
            userService.registerNewUser(registrationDto);

            // Если успешно - сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация успешна! Теперь можете войти.");
            return "redirect:/auth?success";

        } catch (RuntimeException e) {
            // Если ошибка - показываем сообщение
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("userRegistrationDto", registrationDto);
            return "redirect:/auth?error";
        }
    }
}