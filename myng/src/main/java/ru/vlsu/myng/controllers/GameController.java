package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vlsu.myng.dto.MyGame;
import ru.vlsu.myng.dto.PublishGameRequest;
import ru.vlsu.myng.services.GameService;
import ru.vlsu.myng.services.UserService;
import ru.vlsu.myng.utils.ValidationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;


    @GetMapping("/")
    public String indexPage() {
        return "game";
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
            @ModelAttribute PublishGameRequest request) {

        System.out.println("/games/publish reached");

        Map<String, Object> response = new HashMap<>();

        // Validate the request
        Map<String, String> errors = ValidationUtils.validatePublishRequest(request);

        errors.forEach((k, v) -> System.out.println(k + " " + v));

        // If there are validation errors, return them
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // TODO: Implement game publishing logic here

            response.put("success", true);
            response.put("message", "Game published successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to publish game: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}