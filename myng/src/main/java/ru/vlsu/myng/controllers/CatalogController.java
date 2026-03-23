package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.vlsu.myng.dto.CatalogGameDTO;
import ru.vlsu.myng.services.GameService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final GameService gameService;

    @GetMapping("/catalog")
    public String catalogPage(Model model) {
        List<CatalogGameDTO> games = gameService.getAllGamesForCatalog();
        model.addAttribute("games", games);

        return "catalog";
    }
}