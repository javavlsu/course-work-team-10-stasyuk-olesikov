package ru.vlsu.myng.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.entities.*;
import ru.vlsu.myng.repositories.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ModerationLogServiceTest extends BaseIntegrationTest {

    @Autowired
    private ModerationLogService moderationLogService;

    @Autowired
    private ModerationVerdictRepository verdictRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameVersionRepository gameVersionRepository;

    @Autowired
    private DevApplicationRepository devApplicationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User moderator1;
    private User moderator2;
    private User developer1;
    private User developer2;
    private User player1;
    private User player2;

    private Game game1;
    private Game game2;

    private GameVersion gameVersion1;
    private GameVersion gameVersion2;

    private DevApplication devApplication1;
    private DevApplication devApplication2;

    private Review review1;
    private Review review2;

    @BeforeEach
    void setUp() {
        moderator1 = createUser("moderator1", "mod1@mail.com", User.Role.mod);
        moderator2 = createUser("moderator2", "mod2@mail.com", User.Role.mod);
        developer1 = createUser("developer1", "dev1@mail.com", User.Role.dev);
        developer2 = createUser("developer2", "dev2@mail.com", User.Role.dev);
        player1 = createUser("player1", "player1@mail.com", User.Role.user);
        player2 = createUser("player2", "player2@mail.com", User.Role.user);

        game1 = createGame("Epic RPG", developer1, "https://github.com/dev1/epic-rpg");
        game2 = createGame("Space Shooter", developer2, "https://github.com/dev2/space-shooter");

        gameVersion1 = createGameVersion(game1, "v1.0.0", "abc111", "First release", "index.html");
        gameVersion2 = createGameVersion(game2, "v2.0.0", "def222", "Major update", "index.html");

        devApplication1 = createDevApplication(player1, "I want to publish games", "player1_github");
        devApplication2 = createDevApplication(player2, "Experienced developer", "player2_github");

        review1 = createReview(game1, player1, "Great game!", (byte) 5, 2);
        review2 = createReview(game2, player2, "Needs improvement", (byte) 3, 0);

        createVerdict(gameVersion1, null, null, moderator1, true, "Looks good");
        createVerdict(null, devApplication1, null, moderator1, false, "Not enough experience");
        createVerdict(null, null, review1, moderator2, true, null);
        createVerdict(gameVersion2, null, null, moderator2, false, "Contains malware");
        createVerdict(null, devApplication2, null, null, null, null); // pending
        createVerdict(null, null, review2, null, null, null); // pending
    }

    // ==========================================
    // ТЕСТЫ ДЛЯ getModerationItems() (БЕЗ ФИЛЬТРОВ)
    // ==========================================

    @Test
    @DisplayName("Получение всех элементов модерации без фильтров")
    void getModerationItems_NoFilters_ShouldReturnAllItems() {
        List<ModerationItem> items = moderationLogService.getModerationItems();

        assertThat(items).hasSize(6);
        assertThat(items.get(0).getCreatedAt())
                .isAfterOrEqualTo(items.get(1).getCreatedAt());
    }

    // ==========================================
    // ТЕСТЫ ДЛЯ getModerationItems() (С ФИЛЬТРАМИ И ПАГИНАЦИЕЙ)
    // ==========================================

    @Test
    @DisplayName("Фильтрация по типу: GAME_VERSION")
    void getModerationItems_FilterByType_GameVersion() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, "GAME_VERSION", null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> item.getType().equals("GAME_VERSION"));
        assertThat(result.getContent())
                .extracting(ModerationItem::getCommitHash)
                .contains("abc111", "def222");
    }

    @Test
    @DisplayName("Фильтрация по типу: DEV_APPLICATION")
    void getModerationItems_FilterByType_DevApplication() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, "DEV_APPLICATION", null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> item.getType().equals("DEV_APPLICATION"));
        assertThat(result.getContent())
                .extracting(ModerationItem::getUsername)
                .contains("player1", "player2");
    }

    @Test
    @DisplayName("Фильтрация по типу: REVIEW")
    void getModerationItems_FilterByType_Review() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, "REVIEW", null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> item.getType().equals("REVIEW"));
    }

    @Test
    @DisplayName("Фильтрация по статусу: approved")
    void getModerationItems_FilterByStatus_Approved() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, "approved", null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> item.getApproved() != null && item.getApproved());
    }

    @Test
    @DisplayName("Фильтрация по статусу: rejected")
    void getModerationItems_FilterByStatus_Rejected() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, "rejected", null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> item.getApproved() != null && !item.getApproved());
        assertThat(result.getContent())
                .extracting(ModerationItem::getReason)
                .contains("Not enough experience", "Contains malware");
    }

    @Test
    @DisplayName("Фильтрация по статусу: pending")
    void getModerationItems_FilterByStatus_Pending() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, "pending", null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> item.getApproved() == null);
    }

    @Test
    @DisplayName("Поиск по имени модератора")
    void getModerationItems_SearchByModeratorUsername() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                "moderator1", null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(item -> "moderator1".equals(item.getModeratorUsername()));
    }

    @Test
    @DisplayName("Поиск по имени пользователя (для DEV_APPLICATION)")
    void getModerationItems_SearchByUsername() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                "player1", null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo("DEV_APPLICATION");
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("player1");
    }

    @Test
    @DisplayName("Поиск по хешу коммита")
    void getModerationItems_SearchByCommitHash() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                "abc111", null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCommitHash()).isEqualTo("abc111");
    }

    @Test
    @DisplayName("Поиск по причине отклонения")
    void getModerationItems_SearchByReason() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                "malware", null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getReason()).contains("malware");
    }

    @Test
    @DisplayName("Фильтрация по периоду: today")
    void getModerationItems_FilterByPeriod_Today() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, null, "today",
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(6);
    }

    @Test
    @DisplayName("Фильтрация по периоду: week")
    void getModerationItems_FilterByPeriod_Week() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, null, "week",
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(6);
    }

    @Test
    @DisplayName("Пагинация: первая страница")
    void getModerationItems_Pagination_FirstPage() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, null, null,
                PageRequest.of(0, 3));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    @DisplayName("Пагинация: вторая страница")
    void getModerationItems_Pagination_SecondPage() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, null, null, null,
                PageRequest.of(1, 3));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("Комбинированная фильтрация: тип + статус + период")
    void getModerationItems_CombinedFilters() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                null, "GAME_VERSION", "approved", "today",
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo("GAME_VERSION");
        assertThat(result.getContent().get(0).getApproved()).isTrue();
        assertThat(result.getContent().get(0).getCommitHash()).isEqualTo("abc111");
    }

    @Test
    @DisplayName("Комбинированная фильтрация: поиск + тип")
    void getModerationItems_SearchAndTypeFilter() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                "moderator2", "GAME_VERSION", null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getModeratorUsername()).isEqualTo("moderator2");
        assertThat(result.getContent().get(0).getType()).isEqualTo("GAME_VERSION");
        assertThat(result.getContent().get(0).getCommitHash()).isEqualTo("def222");
    }

    @Test
    @DisplayName("Пустой результат при несуществующем поиске")
    void getModerationItems_SearchNoResults() {
        Page<ModerationItem> result = moderationLogService.getModerationItems(
                "nonexistent_user_xyz", null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private User createUser(String username, String email, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setRole(role);
        user.setRegisteredAt(Instant.now());
        user.setProfilePic(new byte[0]);
        return userRepository.save(user);
    }

    private Game createGame(String name, User developer, String repo) {
        Game game = new Game();
        game.setName(name);
        game.setDescr("Description of " + name);
        game.setGenre(Game.Genre.action);
        game.setDeveloper(developer);
        game.setRepo(repo);
        game.setAverageRating(0.0);
        game.setTotalLaunches(0);
        game.setTotalViews(0);
        game.setRatingSum(0);
        game.setReviewCount(0);
        return gameRepository.save(game);
    }

    private GameVersion createGameVersion(Game game, String versionName,
            String commitHash, String changelog, String entryPoint) {
        GameVersion version = new GameVersion();
        version.setGame(game);
        version.setName(versionName);
        version.setCommitHash(commitHash);
        version.setChangelog(changelog);
        version.setFiles("game.exe");
        version.setCreatedAt(Instant.now());
        version.setEntryPoint(entryPoint);
        return gameVersionRepository.save(version);
    }

    private DevApplication createDevApplication(User user, String text, String githubUsername) {
        DevApplication app = new DevApplication();
        app.setUser(user);
        app.setText(text);
        app.setGithubUsername(githubUsername);
        app.setCreatedAt(Instant.now());
        return devApplicationRepository.save(app);
    }

    private Review createReview(Game game, User user, String text, byte rating, int reportCount) {
        Review review = new Review();
        review.setGame(game);
        review.setUser(user);
        review.setText(text);
        review.setRating(rating);
        review.setReportCount(reportCount);
        review.setCreatedAt(Instant.now());
        return reviewRepository.save(review);
    }

    private ModerationVerdict createVerdict(
            GameVersion gameVersion,
            DevApplication devApplication,
            Review review,
            User moderator,
            Boolean approved,
            String reason) {

        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setGameVersion(gameVersion);
        verdict.setDevApplication(devApplication);
        verdict.setReview(review);
        verdict.setModerator(moderator);
        verdict.setApproved(approved);
        verdict.setReason(reason);
        return verdictRepository.save(verdict);
    }
}