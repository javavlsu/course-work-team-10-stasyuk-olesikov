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
        // 1. Популярная игра (больше всего запусков)
        model.addAttribute("featuredGame", gameService.getMostLaunchedGame());

        // 2. Новинка (самая последняя по дате)
        model.addAttribute("newestGame", gameService.getNewestGame());

        // 3. Лучшее за месяц (лучшая средняя оценка за месяц)
        model.addAttribute("bestOfMonth", gameService.getTopRatingGameMonth());

        // 4. Список популярных игр для мини-каталога (все равно, что каталог с сортировкой по популярности)
        model.addAttribute("popularGames", gameService.getPopularGames(6));

        return "index";
    }
}