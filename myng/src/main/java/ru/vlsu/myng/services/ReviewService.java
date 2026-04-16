package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import ru.vlsu.myng.dto.ReviewUpdate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.Review;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.ReviewRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public void updateReview(Integer reviewId, ReviewUpdate dto) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();

        review.setText(dto.getText());
        review.setRating(dto.getRating());

        reviewRepository.save(review);
    }

    public void deleteReview(Integer reviewId) {
        reviewRepository.deleteById(reviewId);
    }

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

    /**
     * Проверяет, оставлял ли пользователь отзыв на эту игру
     */
    @Transactional(readOnly = true)
    public boolean hasUserReviewedGame(Game game, User user) {
        return reviewRepository.existsByGameAndUser(game, user);
    }

    /**
     * Создает новый отзыв
     */
    @Transactional
    public Review createReview(Game game, User user, Byte rating, String text) {
        Review review = new Review();
        review.setGame(game);
        review.setUser(user);
        review.setRating(rating);
        review.setText(text.trim());
        review.setCreatedAt(Instant.now());
        review.setReportCount(0);

        return reviewRepository.save(review);
    }
}