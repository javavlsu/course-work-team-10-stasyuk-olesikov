package ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.vlsu.myng.services.GameService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final GameService gameService;

    @GetMapping({ "/", "/index" })
    public String indexPage(Model model) {
        model.addAttribute("featuredGame", gameService.getMostLaunchedGame());
        model.addAttribute("newestGame", gameService.getNewestGame());
        model.addAttribute("bestOfMonth", gameService.getTopRatingGameMonth());
        model.addAttribute("popularGames", gameService.getPopularGames(6));

        return "index";
    }
}