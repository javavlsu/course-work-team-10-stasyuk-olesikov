package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ru.vlsu.myng.services.ReviewService;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/user/{userId}")
    public String getUserReviews(@PathVariable Integer userId, Model model) {
        model.addAttribute("reviews", reviewService.getByUser(userId));
        return "fragments/reviews :: reviewsFragment";
    }
}