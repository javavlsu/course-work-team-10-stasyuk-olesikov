package ru.vlsu.myng.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.myng.entities.*;
import ru.vlsu.myng.repositories.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ModerationVerdictServiceTest extends BaseIntegrationTest {

    @Autowired
    private ModerationVerdictService moderationVerdictService;

    @Autowired
    private ModerationVerdictRepository moderationVerdictRepository;

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

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private GithubService githubService;

    @MockBean
    private UserService userService;

    private User moderator;
    private User developer;
    private User regularUser;
    private Game game;
    private GameVersion gameVersion;
    private DevApplication devApplication;
    private Review review;

    @BeforeEach
    void setUp() {
        doNothing().when(githubService).downloadGameVersion(any(GameVersion.class));

        moderator = createUser("moderator", "mod@mail.com", User.Role.mod);
        developer = createUser("developer", "dev@mail.com", User.Role.user);
        regularUser = createUser("player", "player@mail.com", User.Role.user);

        when(userService.findByEmail(moderator.getEmail())).thenReturn(moderator);

        game = new Game();
        game.setName("Test Game");
        game.setDescr("A test game description");
        game.setGenre(Game.Genre.action);
        game.setDeveloper(developer);
        game.setRepo("https://github.com/test/game");
        game.setAverageRating(0.0);
        game.setTotalLaunches(0);
        game.setTotalViews(0);
        game.setRatingSum(0);
        game.setReviewCount(0);
        game = gameRepository.save(game);

        gameVersion = new GameVersion();
        gameVersion.setGame(game);
        gameVersion.setName("v1.0.0");
        gameVersion.setCommitHash("abc123def456");
        gameVersion.setFiles("game.exe, data.zip");
        gameVersion.setCreatedAt(Instant.now());
        gameVersion.setEntryPoint("index.html");
        gameVersion = gameVersionRepository.save(gameVersion);

        devApplication = new DevApplication();
        devApplication.setUser(regularUser);
        devApplication.setText("I want to become a developer");
        devApplication.setGithubUsername("player_github");
        devApplication.setCreatedAt(Instant.now());
        devApplication = devApplicationRepository.save(devApplication);

        review = new Review();
        review.setGame(game);
        review.setUser(regularUser);
        review.setText("Great game!");
        review.setRating((byte) 5);
        review.setCreatedAt(Instant.now());
        review.setReportCount(10);
        review = reviewRepository.save(review);
    }

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

    // ==========================================
    // ТЕСТЫ ДЛЯ APPROVE (ОДОБРЕНИЕ)
    // ==========================================

    @Test
    @DisplayName("Одобрение версии игры")
    void approve_GameVersion_ShouldApproveAndNotifyDeveloper() {
        ModerationVerdict verdict = createVerdictForGameVersion(gameVersion);
        var springUser = createSpringUser(moderator.getEmail());

        moderationVerdictService.approve(verdict.getId(), springUser);

        ModerationVerdict updatedVerdict = moderationVerdictRepository.findById(verdict.getId()).orElseThrow();
        assertThat(updatedVerdict.getApproved()).isTrue();
        assertThat(updatedVerdict.getModerator().getId()).isEqualTo(moderator.getId());

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        Notification notification = notifications.get(0);
        assertThat(notification.getType()).isEqualTo(Notification.Type.moderation);
        assertThat(notification.getText()).contains("одобрена");
        assertThat(notification.getText()).contains(game.getName());
        assertThat(notification.getText()).contains(moderator.getUsername());
        assertThat(notification.getUsers()).contains(developer);
    }

    @Test
    @DisplayName("Одобрение заявки на разработчика")
    void approve_DevApplication_ShouldApproveAndPromoteUser() {
        ModerationVerdict verdict = createVerdictForDevApplication(devApplication);
        var springUser = createSpringUser(moderator.getEmail());

        moderationVerdictService.approve(verdict.getId(), springUser);

        ModerationVerdict updatedVerdict = moderationVerdictRepository.findById(verdict.getId()).orElseThrow();
        assertThat(updatedVerdict.getApproved()).isTrue();

        User promotedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertThat(promotedUser.getRole()).isEqualTo(User.Role.dev);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getText()).contains("разработчика");
        assertThat(notifications.get(0).getUsers()).contains(regularUser);
    }

    @Test
    @DisplayName("Одобрение отзыва")
    void approve_Review_ShouldApproveAndResetReportCount() {
        ModerationVerdict verdict = createVerdictForReview(review);
        var moderatorDetails = createSpringUser(moderator.getEmail());

        moderationVerdictService.approve(verdict.getId(), moderatorDetails);

        ModerationVerdict updatedVerdict = moderationVerdictRepository.findById(verdict.getId()).orElseThrow();
        assertThat(updatedVerdict.getApproved()).isTrue();

        Review updatedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updatedReview.getReportCount()).isZero();

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getText()).contains("не удален");
        assertThat(notifications.get(0).getUsers()).contains(regularUser);
    }

    @Test
    @DisplayName("Ошибка при одобрении уже решённого вердикта")
    void approve_AlreadyDecided_ShouldThrowException() {
        ModerationVerdict verdict = createVerdictForGameVersion(gameVersion);
        verdict.setApproved(true);
        moderationVerdictRepository.save(verdict);
        var moderatorDetails = createSpringUser(moderator.getEmail());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            moderationVerdictService.approve(verdict.getId(), moderatorDetails);
        });
        assertThat(exception.getMessage()).isEqualTo("Verdict already decided");
    }

    // ==========================================
    // ТЕСТЫ ДЛЯ REJECT (ОТКЛОНЕНИЕ)
    // ==========================================

    @Test
    @DisplayName("Отклонение версии игры")
    void reject_GameVersion_ShouldRejectAndNotifyDeveloper() {
        ModerationVerdict verdict = createVerdictForGameVersion(gameVersion);
        var springUser = createSpringUser(moderator.getEmail());
        String reason = "Game contains malware";

        moderationVerdictService.reject(verdict.getId(), reason, springUser);

        ModerationVerdict updatedVerdict = moderationVerdictRepository.findById(verdict.getId()).orElseThrow();
        assertThat(updatedVerdict.getApproved()).isFalse();
        assertThat(updatedVerdict.getReason()).isEqualTo(reason);
        assertThat(updatedVerdict.getModerator().getId()).isEqualTo(moderator.getId());
    }

    @Test
    @DisplayName("Отклонение заявки на разработчика")
    void reject_DevApplication_ShouldRejectAndNotify() {
        ModerationVerdict verdict = createVerdictForDevApplication(devApplication);
        var moderatorDetails = createSpringUser(moderator.getEmail());
        String reason = "Not enough experience";

        moderationVerdictService.reject(verdict.getId(), reason, moderatorDetails);

        ModerationVerdict updatedVerdict = moderationVerdictRepository.findById(verdict.getId()).orElseThrow();
        assertThat(updatedVerdict.getApproved()).isFalse();
        assertThat(updatedVerdict.getReason()).isEqualTo(reason);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getText()).contains("отклонена");
        assertThat(notifications.get(0).getText()).contains(reason);
        assertThat(notifications.get(0).getUsers()).contains(regularUser);
    }

    @Test
    @DisplayName("Отклонение отзыва")
    void reject_Review_ShouldRejectAndNotify() {
        ModerationVerdict verdict = createVerdictForReview(review);
        var moderatorDetails = createSpringUser(moderator.getEmail());
        String reason = "Inappropriate content";

        moderationVerdictService.reject(verdict.getId(), reason, moderatorDetails);

        ModerationVerdict updatedVerdict = moderationVerdictRepository.findById(verdict.getId()).orElseThrow();
        assertThat(updatedVerdict.getApproved()).isFalse();
        assertThat(updatedVerdict.getReason()).isEqualTo(reason);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getText()).contains("скрыт навсегда");
        assertThat(notifications.get(0).getText()).contains(reason);
        assertThat(notifications.get(0).getUsers()).contains(regularUser);
    }

    @Test
    @DisplayName("Ошибка при отклонении уже решённого вердикта")
    void reject_AlreadyDecided_ShouldThrowException() {
        ModerationVerdict verdict = createVerdictForGameVersion(gameVersion);
        verdict.setApproved(false);
        moderationVerdictRepository.save(verdict);
        var moderatorDetails = createSpringUser(moderator.getEmail());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            moderationVerdictService.reject(verdict.getId(), "Bad game", moderatorDetails);
        });
        assertThat(exception.getMessage()).isEqualTo("Verdict already decided");
    }

    // ==========================================
    // ТЕСТЫ ДЛЯ SAVE И FIND
    // ==========================================

    @Test
    @DisplayName("Сохранение вердикта")
    void save_ShouldPersistVerdict() {
        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setGameVersion(gameVersion);

        ModerationVerdict savedVerdict = moderationVerdictService.save(verdict);

        assertThat(savedVerdict.getId()).isNotNull();
        assertThat(moderationVerdictRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Поиск вердикта по заявке разработчика")
    void findByDevApplication_ShouldReturnVerdict() {
        ModerationVerdict verdict = createVerdictForDevApplication(devApplication);

        Optional<ModerationVerdict> found = moderationVerdictService.findByDevApplication(devApplication);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(verdict.getId());
    }

    @Test
    @DisplayName("Поиск несуществующего вердикта")
    void findByDevApplication_ShouldReturnEmpty() {
        DevApplication newApp = new DevApplication();
        newApp.setUser(moderator);
        newApp.setText("Test");
        newApp = devApplicationRepository.save(newApp);

        Optional<ModerationVerdict> found = moderationVerdictService.findByDevApplication(newApp);

        assertThat(found).isEmpty();
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private ModerationVerdict createVerdictForGameVersion(GameVersion version) {
        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setGameVersion(version);
        return moderationVerdictRepository.save(verdict);
    }

    private ModerationVerdict createVerdictForDevApplication(DevApplication app) {
        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setDevApplication(app);
        return moderationVerdictRepository.save(verdict);
    }

    private ModerationVerdict createVerdictForReview(Review review) {
        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setReview(review);
        return moderationVerdictRepository.save(verdict);
    }

    private org.springframework.security.core.userdetails.User createSpringUser(String email) {
        return new org.springframework.security.core.userdetails.User(
                email,
                "password",
                List.of());
    }
}