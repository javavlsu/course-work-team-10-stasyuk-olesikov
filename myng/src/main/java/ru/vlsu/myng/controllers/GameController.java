package ru.vlsu.myng.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.vlsu.myng.dto.GamePageDTO;
import ru.vlsu.myng.dto.MyGame;
import ru.vlsu.myng.dto.PublishGameRequest;
import ru.vlsu.myng.services.GameService;
import ru.vlsu.myng.services.ReviewService;
import ru.vlsu.myng.services.UserService;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.User;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public String getGamePage(@PathVariable Integer id, Model model, Principal principal) {
        try {
            GamePageDTO game = gameService.getGamePageData(id);
            model.addAttribute("game", game);

            // Получаем текущего пользователя
            User currentUser = null;
            boolean isAuthenticated = false;

            if (principal != null) {
                String email = principal.getName();
                currentUser = userService.findByEmail(email);
                isAuthenticated = currentUser != null;
            }

            // Проверяем, является ли текущий пользователь разработчиком игры
            boolean isDeveloper = isAuthenticated &&
                    currentUser != null &&
                    game.getDeveloper() != null &&
                    currentUser.getId().equals(game.getDeveloper().getId());

            // Проверял ли пользователь уже отзыв
            boolean hasUserReviewed = false;
            if (isAuthenticated && currentUser != null) {
                Game gameEntity = gameService.getGameById(id);
                hasUserReviewed = reviewService.hasUserReviewedGame(gameEntity, currentUser);
            }

            model.addAttribute("isAuthenticated", isAuthenticated);
            model.addAttribute("isDeveloper", isDeveloper);
            model.addAttribute("hasUserReviewed", hasUserReviewed);
            model.addAttribute("isDev", currentUser != null && currentUser.getRole() == User.Role.dev);
            model.addAttribute("currentUserId", currentUser != null ? currentUser.getId() : null);

            return "game";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable Integer id,
            @RequestParam Byte rating,
            @RequestParam String text,
            @RequestParam(required = false) String gameId,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            Game game = gameService.getGameById(id);

            // Проверки
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Необходимо авторизоваться");
                return "redirect:/auth";
            }

            if (game.getDeveloper().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Разработчик не может оставлять отзывы к своей игре");
                return "redirect:/games/" + id;
            }

            if (reviewService.hasUserReviewedGame(game, currentUser)) {
                redirectAttributes.addFlashAttribute("error", "Вы уже оставили отзыв к этой игре");
                return "redirect:/games/" + id;
            }

            if (rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Некорректная оценка");
                return "redirect:/games/" + id;
            }

            if (text == null || text.trim().length() < 10) {
                redirectAttributes.addFlashAttribute("error", "Текст отзыва должен содержать минимум 10 символов");
                return "redirect:/games/" + id;
            }

            reviewService.createReview(game, currentUser, rating, text);
            redirectAttributes.addFlashAttribute("success", "Отзыв успешно добавлен!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении отзыва: " + e.getMessage());
        }

        return "redirect:/games/" + id;
    }

    @PostMapping("/reviews/{reviewId}/report")
    @ResponseBody
    public ResponseEntity<?> reportReview(@PathVariable Integer reviewId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Нужна авторизация");
        }

        User currentUser = userService.findByEmail(principal.getName());
        Review review = reviewService.getReviewById(reviewId);

        if (review.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("Нельзя жаловаться на свой отзыв");
        }

        reviewService.incrementReportCount(reviewId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/reviews/more")
    public String getMoreReviews(@PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal) {
        Game game = gameService.getGameById(id);
        List<Review> moreReviews = reviewService.findByGameOrderByCreatedAtDesc(
                game,
                PageRequest.of(page, 5));

        // Передаем ID пользователя, чтобы кнопка "Пожаловаться" работала корректно
        User currentUser = (principal != null) ? userService.findByEmail(principal.getName()) : null;
        model.addAttribute("currentUserId", currentUser != null ? currentUser.getId() : null);
        model.addAttribute("isAuthenticated", principal != null);
        model.addAttribute("reviews", moreReviews);

        // Возвращаем только фрагмент списка
        return "fragments/review_items :: reviewList";
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

        gameService.publishGame(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Game published successfully"));
    }
}