package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import ru.vlsu.myng.dto.ReviewUpdate;

import org.springframework.data.domain.PageRequest;
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

    /**
     * Получить отзыв по ID
     */
    @Transactional(readOnly = true)
    public Review getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
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

    /**
     * Увеличить жалобы на отзыв
     */
    @Transactional()
    public void incrementReportCount(Integer id) {
        Review review = getReviewById(id);
        review.setReportCount(review.getReportCount() + 1);
    }

    /**
     * Получить отзывы по игре
     */
    @Transactional(readOnly = true)
    public List<Review> findByGameOrderByCreatedAtDesc(Game game, PageRequest pageable) {
        return reviewRepository.findByGameOrderByCreatedAtDesc(game, pageable);
    }
}