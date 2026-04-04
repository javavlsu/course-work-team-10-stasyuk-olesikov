package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ru.vlsu.myng.dto.MyGame;
import ru.vlsu.myng.entities.*;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.repositories.GameStatsRepository;
import ru.vlsu.myng.repositories.GameVersionRepository;
import ru.vlsu.myng.repositories.ReviewRepository;
import ru.vlsu.myng.repositories.UserRepository;
import ru.vlsu.myng.repositories.TagRepository;
import ru.vlsu.myng.dto.CatalogGameDTO;
import ru.vlsu.myng.dto.GameFilterDTO;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
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
        Double avgRating = reviewRepository.getAverageRatingByGame(game);
        Integer reviewsCount = reviewRepository.countByGame(game);

        Integer totalViews = gameStatsRepository.getTotalStatsByGameAndType(game, GameStats.EventType.view);
        Integer totalLaunches = gameStatsRepository.getTotalStatsByGameAndType(game, GameStats.EventType.launch);

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
        switch (genre) {
            case action:
                return "from-red-500 to-orange-600";
            case adventure:
                return "from-yellow-500 to-orange-600";
            case rpg:
                return "from-purple-500 to-pink-600";
            case simulation:
                return "from-green-500 to-emerald-600";
            case strategy:
                return "from-blue-500 to-cyan-600";
            case sports:
                return "from-green-500 to-blue-600";
            case puzzle:
                return "from-indigo-500 to-purple-600";
            case horror:
                return "from-gray-700 to-gray-900";
            case platformer:
                return "from-cyan-500 to-blue-600";
            case sandbox:
                return "from-yellow-500 to-red-600";
            case visual_novel:
                return "from-pink-500 to-purple-600";
            case roguelike:
                return "from-orange-500 to-red-600";
            default:
                return "from-indigo-500 to-purple-600";
        }
    }

    @Transactional(readOnly = true)
    public Page<CatalogGameDTO> getFilteredGames(GameFilterDTO filter, Pageable pageable) {
        // 1. Получаем все игры с базовыми фильтрами (поиск, жанр)
        List<Game> games = gameRepository.findGamesWithBasicFilters(
                filter.getSearch(),
                filter.getGenre());

        // 2. Конвертируем в DTO (чтобы получить рейтинг и теги)
        List<CatalogGameDTO> dtos = games.stream()
                .map(this::convertToCatalogDto)
                .collect(Collectors.toList());

        // 3. Фильтрация по тегам (в Java)
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getTags() != null &&
                            dto.getTags().stream().anyMatch(filter.getTags()::contains))
                    .collect(Collectors.toList());
        }

        // 4. Фильтрация по рейтингу (в Java)
        if (filter.getMinRating() != null) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getAverageRating() >= filter.getMinRating())
                    .collect(Collectors.toList());
        }

        // 5. Сортировка (в Java)
        dtos = sortGames(dtos, filter.getSort());

        // 6. Пагинация (в Java)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        List<CatalogGameDTO> pagedList = start < end ? dtos.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(pagedList, pageable, dtos.size());
    }

    private List<CatalogGameDTO> sortGames(List<CatalogGameDTO> games, String sort) {
        if (sort == null)
            return games;

        switch (sort) {
            case "newest":
                return games.stream()
                        .sorted((a, b) -> {
                            if (a.getFirstReleaseDate() == null)
                                return 1;
                            if (b.getFirstReleaseDate() == null)
                                return -1;
                            return b.getFirstReleaseDate().compareTo(a.getFirstReleaseDate());
                        })
                        .collect(Collectors.toList());
            case "oldest":
                return games.stream()
                        .sorted((a, b) -> {
                            if (a.getFirstReleaseDate() == null)
                                return 1;
                            if (b.getFirstReleaseDate() == null)
                                return -1;
                            return a.getFirstReleaseDate().compareTo(b.getFirstReleaseDate());
                        })
                        .collect(Collectors.toList());
            case "rating_high":
                return games.stream()
                        .sorted((a, b) -> b.getAverageRating().compareTo(a.getAverageRating()))
                        .collect(Collectors.toList());
            case "rating_low":
                return games.stream()
                        .sorted((a, b) -> a.getAverageRating().compareTo(b.getAverageRating()))
                        .collect(Collectors.toList());
            case "popular":
                return games.stream()
                        .sorted((a, b) -> b.getTotalLaunches().compareTo(a.getTotalLaunches()))
                        .collect(Collectors.toList());
            default:
                return games;
        }
    }

    public List<MyGame> getGamesForUser(User user) {
        List<Game> games = gameRepository.findByDeveloper(user);

        return games.stream().map(game -> {
            GameVersion latestVersion = game.getVersions().stream()
                    .max(Comparator.comparing(GameVersion::getCreatedAt))
                    .orElse(null);

            Boolean approved = null;
            if (latestVersion != null) {
                ModerationVerdict verdict = latestVersion.getModerationVerdict();
                approved = verdict.getApproved();
            }

            String status;
            if (approved != null) {
                status = approved ? "Опубликована" : "Отклонена";
            } else {
                status = "На модерации";
            }

            int views = game.getStats().stream()
                    .filter(s -> s.getEventType() == GameStats.EventType.view)
                    .mapToInt(GameStats::getCount)
                    .sum();

            double rating = game.getReviews().isEmpty() ? 0.0
                    : game.getReviews().stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0);

            return new MyGame(game.getId(), game.getName(), game.getDescr(), approved, status, views, rating);
        }).collect(Collectors.toList());
    }
}