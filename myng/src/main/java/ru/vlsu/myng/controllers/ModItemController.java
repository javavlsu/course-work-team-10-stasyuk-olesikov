package ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vlsu.myng.repositories.DevApplicationRepository;
import ru.vlsu.myng.repositories.ReviewRepository;
import ru.vlsu.myng.services.DevApplicationService;
import ru.vlsu.myng.services.GameService;

import lombok.RequiredArgsConstructor;
import ru.vlsu.myng.services.ReviewService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mod-item")
public class ModItemController {

    private final GameService gameService;
    private final ReviewRepository reviewRepository;
    private final DevApplicationRepository devApplicationRepository;

    @GetMapping("/reviews/{id}")
    public String getModItemReviewPage(@PathVariable Integer id, Model model) {
        var r = reviewRepository.findById(id);
        
        model.addAttribute("review", r.isEmpty() ? null : r.get());
        model.addAttribute("type", "review");
        
        return "mod_item";
    }

    @GetMapping("/dev-apps/{id}")
    public String getModItemDevAppPage(@PathVariable Integer id, Model model) {
        var da = devApplicationRepository.findById(id);
        
        model.addAttribute("devapp", da.isEmpty() ? null : da.get());
        model.addAttribute("type", "devapp");

        return "mod_item";
    }
}