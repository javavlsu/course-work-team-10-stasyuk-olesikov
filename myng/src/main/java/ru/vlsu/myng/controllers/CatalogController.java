package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.vlsu.myng.dto.CatalogGameDTO;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.services.GameService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final GameService gameService;
    // Временно
    private final GameRepository gameRepository;

    @GetMapping("/catalog")
    public String catalogPage(Model model) {
        List<CatalogGameDTO> games = gameService.getAllGamesForCatalog();
        model.addAttribute("games", games);

        return "catalog";
    }
}