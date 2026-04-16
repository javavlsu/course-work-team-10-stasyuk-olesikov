package ru.vlsu.myng.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ru.vlsu.myng.dto.GamePageDTO;
import ru.vlsu.myng.dto.MyGame;
import ru.vlsu.myng.dto.PublishGameRequest;
import ru.vlsu.myng.services.GameService;
import ru.vlsu.myng.services.UserService;
import ru.vlsu.myng.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String getGamePage(@PathVariable Integer id, Model model) {
        try {
            System.out.println("=== DEBUG: Getting game with id: " + id);
            GamePageDTO game = gameService.getGamePageData(id);
            System.out.println("=== DEBUG: Game found: " + game.getName());

            model.addAttribute("game", game);
            model.addAttribute("isDev", userService.isCurrentUserDev());

            return "game";
        } catch (Exception e) {
            System.err.println("=== ERROR: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @GetMapping("/developer/{userId}")
    public String getDeveloperGames(@PathVariable Integer userId, Model model) {
        var user = userService.findById(userId);
        List<MyGame> games = gameService.getGamesForUser(user);
        model.addAttribute("mygames", games);
        return "fragments/my_games :: myGamesFragment";
    }

    @PostMapping("/publish")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> publishGame(
            @Valid @ModelAttribute PublishGameRequest request) {

        Map<String, Object> response = new HashMap<>();

        gameService.publishGame(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Game published successfully"));
    }
}