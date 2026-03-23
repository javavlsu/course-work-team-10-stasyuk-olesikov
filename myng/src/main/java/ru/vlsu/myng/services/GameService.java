package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.Tag;
import ru.vlsu.myng.entities.GameStats;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.repositories.GameStatsRepository;
import ru.vlsu.myng.repositories.GameVersionRepository;
import ru.vlsu.myng.repositories.ReviewRepository;
import ru.vlsu.myng.repositories.UserRepository;
import ru.vlsu.myng.repositories.TagRepository;
import ru.vlsu.myng.dto.CatalogGameDTO;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameStatsRepository gameStatsRepository;
    private final ReviewRepository reviewRepository;
    private final GameVersionRepository gameVersionRepository;
    private final UserRepository userRepository;

    public List<Game> getDeveloperGames(Integer userId) {
        User developer = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found"));

        return gameRepository.findByDeveloper(developer);
    }

    public Game getGameByRepo(String repo) {
        return gameRepository.findByRepo(repo)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    public List<Game> getGamesByGenre(Game.Genre genre) {
        return gameRepository.findByGenre(genre);
    }

    public boolean repoExists(String repo) {
        return gameRepository.existsByRepo(repo);
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    /**
     * Получить все игры для каталога с дополнительной информацией
     */
    @Transactional(readOnly = true)
    public List<CatalogGameDTO> getAllGamesForCatalog() {
        // Получаем уже инициализированные объекты
        List<Game> games = gameRepository.findAllForCatalog();

        return games.stream()
                .map(this::convertToCatalogDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить игру по ID
     */
    @Transactional(readOnly = true)
    public Game getGameById(Integer id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Игра не найдена"));
    }

    /**
     * Конвертирует Game в CatalogGameDTO с дополнительными данными
     */
    private CatalogGameDTO convertToCatalogDto(Game game) {
        // Получаем средний рейтинг и количество отзывов через репозиторий
        Double avgRating = reviewRepository.getAverageRatingByGame(game);
        Integer reviewsCount = reviewRepository.countByGame(game);

        // Получаем статистику просмотров и запусков
        Integer totalViews = gameStatsRepository.getTotalStatsByGameAndType(game, GameStats.EventType.view);
        Integer totalLaunches = gameStatsRepository.getTotalStatsByGameAndType(game, GameStats.EventType.launch);

        // Получаем дату первого релиза
        Instant firstReleaseDate = gameVersionRepository.findFirstByGameOrderByCreatedAtAsc(game)
                .map(version -> version.getCreatedAt())
                .orElse(null);

        // Собираем теги, автоматически убирая тот, который дублирует жанр
        String genreName = game.getGenre() != null ? game.getGenre().name() : "";

        Set<String> tags = game.getTags().stream()
                .map(Tag::getName)
                // Исключаем тег, если он совпадает с жанром
                .filter(tagName -> !tagName.equalsIgnoreCase(genreName))
                .collect(Collectors.toSet());

        return CatalogGameDTO.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescr())
                .genre(game.getGenre())
                .developerUsername(game.getDeveloper() != null ? game.getDeveloper().getUsername() : "Unknown")
                .tags(tags)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewsCount(reviewsCount != null ? reviewsCount : 0)
                .totalViews(totalViews != null ? totalViews : 0)
                .totalLaunches(totalLaunches != null ? totalLaunches : 0)
                .firstReleaseDate(firstReleaseDate)
                .themeColor(getGenreColor(game.getGenre()))
                .build();
    }

    private String getGenreColor(Game.Genre genre) {
        if (genre == null)
            return "from-indigo-500 to-purple-600";

        switch (genre.name().toLowerCase()) {
            case "action":
                return "from-red-500 to-orange-600";
            case "adventure":
                return "from-yellow-500 to-orange-600";
            case "rpg":
                return "from-purple-500 to-pink-600";
            case "simulation":
                return "from-green-500 to-emerald-600";
            case "strategy":
                return "from-blue-500 to-cyan-600";
            case "sports":
                return "from-green-500 to-blue-600";
            case "puzzle":
                return "from-indigo-500 to-purple-600";
            case "horror":
                return "from-gray-700 to-gray-900";
            case "platformer":
                return "from-cyan-500 to-blue-600";
            case "sandbox":
                return "from-yellow-500 to-red-600";
            case "visual_novel":
                return "from-pink-500 to-purple-600";
            case "roguelike":
                return "from-orange-500 to-red-600";
            default:
                return "from-indigo-500 to-purple-600";
        }
    }
}