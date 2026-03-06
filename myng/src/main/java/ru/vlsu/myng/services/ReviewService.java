package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.ReviewRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public List<Review> getByUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return reviewRepository.findByUser(user);
    }

    public List<Review> getByGame(Game game) {
        return reviewRepository.findByGame(game);
    }

    public Optional<Review> getUserGameReview(User user, Game game) {
        return reviewRepository.findByUserAndGame(user, game);
    }

    public boolean reviewExists(User user, Game game) {
        return reviewRepository.existsByUserAndGame(user, game);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void delete(Integer id) {
        reviewRepository.deleteById(id);
    }
}