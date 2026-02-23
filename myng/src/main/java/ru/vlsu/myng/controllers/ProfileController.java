package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.UserRepository;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping("/profile/{id}")
    public String profilePage(@PathVariable("id") Integer id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        model.addAttribute("user", user);

        return "profile"; // Thymeleaf template profile.html in templates folder
    }

    @GetMapping("/profile/{id}/avatar")
    @ResponseBody
    public byte[] getAvatar(@PathVariable("id") Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getProfilePic() != null) {
            return user.getProfilePic();
        }

        // Optionally, return a default avatar as byte[]
        return new byte[0];
    }
}