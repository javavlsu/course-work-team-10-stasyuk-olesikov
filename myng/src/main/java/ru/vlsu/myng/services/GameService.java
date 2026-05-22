package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.vlsu.myng.dto.MyGame;
import ru.vlsu.myng.dto.PublishGameRequest;
import ru.vlsu.myng.entities.*;
import ru.vlsu.myng.repositories.*;
import ru.vlsu.myng.dto.CatalogGameDTO;
import ru.vlsu.myng.dto.GameEditRequestDTO;
import ru.vlsu.myng.dto.GameFilterDTO;
import ru.vlsu.myng.dto.GamePageDTO;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameStatsRepository gameStatsRepository;
    private final ReviewRepository reviewRepository;
    private final GameVersionRepository gameVersionRepository;
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final UserRepository userRepository;
    private final GithubService githubService;
    private final UserService userService;
    private final TagService tagService;
    private final TagRepository tagRepository;

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
     * Популярная игра (больше всего запусков за все время)
     */
    @Transactional(readOnly = true)
    public CatalogGameDTO getMostLaunchedGame() {
        List<Game> games = gameRepository.findTopGamesByLaunches(PageRequest.of(0, 1));
        return games.isEmpty() ? null : convertToCatalogDto(games.get(0));
    }

    /**
     * Новинка (самая последняя добавленная версия в системе, имеющая хотя бы одну
     * подтвержденную версию)
     */
    @Transactional(readOnly = true)
    public CatalogGameDTO getNewestGame() {
        var latestVersion = gameVersionRepository.findLatestApproved(PageRequest.of(0, 1))
                .stream().findFirst();
        if (latestVersion.isEmpty())
            return null;
        return convertToCatalogDto(latestVersion.get().getGame());
    }

    /**
     * Лучшее за месяц (высший средний рейтинг по отзывам за последние 30 дней)
     */
    @Transactional(readOnly = true)
    public CatalogGameDTO getTopRatingGameMonth() {
        Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Game> games = reviewRepository.findTopRatedGamesSince(monthAgo, PageRequest.of(0, 1));

        // Если за месяц никто не ставил оценки, берем просто лучшую игру по рейтингу за
        // все время
        if (games.isEmpty()) {
            List<Game> allTimeBest = reviewRepository.findTopRatedGames(PageRequest.of(0, 1));
            return allTimeBest.isEmpty() ? null : convertToCatalogDto(allTimeBest.get(0));
        }

        return convertToCatalogDto(games.get(0));
    }

    /**
     * Список популярных игр для мини-каталога
     */
    @Transactional(readOnly = true)
    public List<CatalogGameDTO> getPopularGames(int limit) {
        GameFilterDTO filter = new GameFilterDTO();
        filter.setSearch(null);
        filter.setTags(null);
        filter.setGenre(null);
        filter.setMinRating(null);
        filter.setSort("popular");

        var catalogGames = getFilteredGames(filter, PageRequest.of(0, limit));
        return catalogGames.getContent();
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
        Double avgRating = game.getAverageRating();
        Integer reviewsCount = game.getReviewCount();
        Integer totalViews = game.getTotalViews();
        Integer totalLaunches = game.getTotalLaunches();
        Instant firstReleaseDate = game.getFirstReleaseDate();

        String genreName = game.getGenre() != null ? game.getGenre().name() : "";

        Set<String> tags = game.getTags().stream()
                .map(Tag::getName)
                .filter(tagName -> !tagName.equalsIgnoreCase(genreName))
                .collect(Collectors.toSet());

        CatalogGameDTO dto = CatalogGameDTO.builder()
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

        if (game.getImage() != null) {
            String base64 = Base64.getEncoder().encodeToString(game.getImage());
            dto.setBase64Image("data:image/jpeg;base64," + base64);
        } else {
            dto.setBase64Image(null);
        }

        return dto;
    }

    private String getGenreColor(Game.Genre genre) {
        if (genre == null)
            return "from-blue-500 to-cyan-600";
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
                return "from-yellow-500 to-red-600";
            case horror:
                return "from-red-500 to-gray-600";
            case platformer:
                return "from-cyan-500 to-blue-600";
            case sandbox:
                return "from-yellow-500 to-red-600";
            case visual_novel:
                return "from-blue-500 to-pink-600";
            case roguelike:
                return "from-orange-500 to-red-600";
            default:
                return "from-blue-500 to-cyan-600";
        }
    }

    @Transactional(readOnly = true)
    public Page<CatalogGameDTO> getFilteredGames(
            GameFilterDTO filter,
            Pageable pageable) {

        Sort sort = buildAggregateSort(filter.getSort());

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort);

        Page<CatalogGameDTO> page = gameRepository.findCatalogGames(
                filter.getSearch(),
                filter.getGenre(),
                filter.getTags(),
                filter.getMinRating(),
                sortedPageable);

        page.getContent().forEach(dto -> {

            dto.setThemeColor(
                    getGenreColor(dto.getGenre()));

            dto.setTags(
                    tagRepository.findTagNamesByGameId(dto.getId()));
        });

        return page;
    }

    private Sort buildAggregateSort(String sort) {

        if (sort == null)
            sort = "newest";

        return switch (sort) {

            case "oldest" ->
                JpaSort.unsafe(
                        Sort.Direction.ASC,
                        "g.firstReleaseDate");

            case "rating_high" ->
                JpaSort.unsafe(
                        Sort.Direction.DESC,
                        "g.averageRating");

            case "rating_low" ->
                JpaSort.unsafe(
                        Sort.Direction.ASC,
                        "g.averageRating");

            case "popular" ->
                JpaSort.unsafe(
                        Sort.Direction.DESC,
                        "g.totalLaunches");

            default ->
                JpaSort.unsafe(
                        Sort.Direction.DESC,
                        "g.firstReleaseDate");
        };
    }

    /**
     * Получить игру для страницы игры с полными данными
     */
    @Transactional(readOnly = true)
    public GamePageDTO getGamePageData(Integer gameId) {

        Game game = getGameById(gameId);

        // Получаем статистику
        Double avgRating = game.getAverageRating();
        Integer reviewsCount = game.getReviewCount();
        Integer totalViews = game.getTotalViews();
        Integer totalLaunches = game.getTotalLaunches();

        // Дата первого релиза
        Instant firstReleaseDate = game.getFirstReleaseDate();

        // Получаем версии
        List<GameVersion> versions = game.getVersions().stream()
                .filter(version -> version.getModerationVerdict() != null &&
                        Boolean.TRUE.equals(
                                version.getModerationVerdict().getApproved()))
                .sorted(Comparator.comparing(GameVersion::getCreatedAt).reversed())
                .collect(Collectors.toList());

        GameVersion latestVersion = versions.isEmpty() ? null : versions.get(0);

        List<Review> recentReviews = reviewRepository.findRecentReviews(
                game.getId(),
                9,
                PageRequest.of(0, 5));

        // Дата последнего обновления
        Instant lastUpdateDate = latestVersion != null ? latestVersion.getCreatedAt() : null;

        // Цвет темы
        String themeColor = getGenreColor(game.getGenre());

        GamePageDTO dto = GamePageDTO.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescr())
                .themeColor(themeColor)
                .genre(game.getGenre())
                .developer(game.getDeveloper())
                .tags(game.getTags())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalViews(totalViews != null ? totalViews : 0)
                .totalLaunches(totalLaunches != null ? totalLaunches : 0)
                .reviewsCount(reviewsCount != null ? reviewsCount : 0)
                .versions(versions)
                .latestVersion(latestVersion)
                .recentReviews(recentReviews)
                .firstReleaseDate(firstReleaseDate)
                .lastUpdateDate(lastUpdateDate)
                .build();

        if (game.getImage() != null && game.getImage().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(game.getImage());
            dto.setBase64Image("data:image/jpeg;base64," + base64);
        } else {
            dto.setBase64Image(null);
        }

        return dto;
    }

    @Transactional
    public void updateGame(Integer gameId, GameEditRequestDTO request) throws IOException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Игра не найдена"));

        if (request.getName() != null && !request.getName().isBlank()) {
            game.setName(request.getName());
        }
        game.setDescr(request.getDescription());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            game.setImage(request.getImage().getBytes());
        }

        gameRepository.save(game);
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

            String imageSrc = null;

            if (game.getImage() != null && game.getImage().length > 0) {

                String base64 = Base64.getEncoder()
                        .encodeToString(game.getImage());

                imageSrc = "data:image/jpeg;base64," + base64;
            }

            String bgClass = getGenreColor(game.getGenre());

            return new MyGame(game.getId(), game.getName(), game.getDescr(), approved, status, views, rating, imageSrc,
                    bgClass);
        }).collect(Collectors.toList());
    }

    public void publishGame(PublishGameRequest dto) {
        /*
         * 1. Check if game already exists via repo parameter. DONE
         * 2. If game doesn't exist already create a Game entity inside the DB;
         * create a GameVersion entity as well; create ModerationVerdict related
         * to that GameVersion.
         * 3. If new tags are present, add them to the Tags table. Link tags and the
         * published game.
         * 4. NOTICE: GameStats entity should be created only after approval of the
         * game;
         * It doesn't make sense to create it right away as nobody will see it anyway.
         * Also, the archive with game files should also be downloaded only after
         * approval.
         */

        /*
         * The repo link must be in the format
         * https://github.com/[github_username]/[repo-name]
         * The game version must be a string of type v<number>.<number>.<number>...
         * The commit_hash must be a 7 digit hexadecimal number
         * Description must be at least 10 and at most 2000 chars long
         * Game's name must be at least 3 and at most 100 chars long
         * File is required, it's a picture and its size must at most be 32 mb
         * The file list is a string of filenames separated by ", " (file0.ext0,
         * file0.ext1, file1.ext0)
         * The tags is a string of tags of format "#this-is-tag-name" separated by ", ".
         * It must have at least one tag.
         */

        githubService.validateRepoExists(dto.getRepoLink());
        githubService.validateCommitExists(dto.getRepoLink(), dto.getCommitHash());
        githubService.validateFilesExistInCommit(dto.getRepoLink(), dto.getCommitHash(), dto.getFiles());

        Game game = new Game();

        game.setName(dto.getTitle());
        game.setDescr(dto.getDescription());
        game.setRepo(dto.getRepoLink());
        try {
            var file = dto.getMainPic();
            if (file != null && !file.isEmpty() && file.getSize() > 0) {
                game.setImage(file.getBytes());
            } else {
                game.setImage(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        game.setGenre(
                Game.Genre.valueOf(dto.getGenre()));

        User currentUser = userService.getCurrentUser();
        game.setDeveloper(currentUser);

        Set<Tag> tags = Arrays.stream(dto.getTags().split(","))
                .map(String::trim)
                .map(tag -> tag.startsWith("#") ? tag.substring(1) : tag)
                .map(String::toLowerCase)
                .map(tagName -> tagService.findOrCreate(tagName))
                .collect(Collectors.toSet());

        game.setTags(tags);

        game.setFirstReleaseDate(Instant.now());

        gameRepository.save(game);

        GameVersion version = new GameVersion();
        version.setGame(game);
        version.setCommitHash(dto.getCommitHash());
        version.setName(dto.getGameVer());
        version.setCreatedAt(game.getFirstReleaseDate());
        version.setFiles(dto.getFiles());

        gameVersionRepository.save(version);

        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setGameVersion(version);
        verdict.setApproved(null);
        verdict.setReason(null);
        verdict.setModerator(null);

        moderationVerdictRepository.save(verdict);

        version.setModerationVerdict(verdict);
    }

    @Transactional
    public void incrementGameTotalLaunches(Game game) {
        game.setTotalLaunches(game.getTotalLaunches() + 1);
        gameRepository.save(game);
    }

    @Transactional
    public void incrementGameTotalLaunches(Integer gameId) {
        var game = getGameById(gameId);
        game.setTotalLaunches(game.getTotalLaunches() + 1);
        gameRepository.save(game);
    }

    @Transactional
    public void incrementGameTotalViews(Game game) {
        game.setTotalViews(game.getTotalViews() + 1);
        gameRepository.save(game);
    }

    @Transactional
    public void incrementGameTotalViews(Integer gameId) {
        var game = getGameById(gameId);
        game.setTotalViews(game.getTotalViews() + 1);
        gameRepository.save(game);
    }
}