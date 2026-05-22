package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ru.vlsu.myng.entities.*;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;
import ru.vlsu.myng.repositories.NotificationRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationVerdictService {
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final GithubService githubService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ModerationVerdict save(ru.vlsu.myng.entities.ModerationVerdict verdict) {
        return moderationVerdictRepository.save(verdict);
    }

    public Optional<ModerationVerdict> findByDevApplication(DevApplication app) {
        return moderationVerdictRepository.findByDevApplication(app);
    }

    @Transactional
    public void approve(Integer moderationVerdictId, org.springframework.security.core.userdetails.User user) {
        ModerationVerdict verdict = moderationVerdictRepository.findById(moderationVerdictId)
                .orElseThrow(() -> new RuntimeException("Moderation verdict not found"));

        if (verdict.getApproved() != null) {
            throw new IllegalStateException("Verdict already decided");
        }

        User moderator = userService.findByEmail(user.getUsername());
        User recipient = null;
        String notificationText = null;
        String gameName = null;

        var gameVersion = verdict.getGameVersion();
        if (gameVersion != null) {
            githubService.downloadGameVersion(gameVersion);
            recipient = gameVersion.getGame().getDeveloper();
            gameName = gameVersion.getGame().getName();
            notificationText = String.format(
                    "Ваша версия игры \"%s\" одобрена модератором @%s и опубликована",
                    gameName,
                    moderator.getUsername());
        }

        var devApp = verdict.getDevApplication();
        if (devApp != null) {
            recipient = devApp.getUser();
            recipient.setRole(User.Role.dev);
            userRepository.save(recipient);
            notificationText = String.format(
                    "Ваша заявка на статус разработчика одобрена модератором @%s! Теперь вы можете публиковать игры",
                    moderator.getUsername());
        }

        var review = verdict.getReview();
        if (review != null) {
            recipient = review.getUser();
            gameName = review.getGame().getName();
            notificationText = String.format(
                    "Ваш отзыв на игру \"%s\" прошел модерацию и не удален",
                    gameName);
            review.setReportCount(0);
        }

        verdict.setApproved(true);
        verdict.setModerator(moderator);
        moderationVerdictRepository.save(verdict);

        if (recipient != null && notificationText != null) {
            createNotification(recipient, notificationText, Notification.Type.moderation);
        }
    }

    @Transactional
    public void reject(Integer moderationVerdictId, String reason,
            org.springframework.security.core.userdetails.User user) {
        ModerationVerdict verdict = moderationVerdictRepository.findById(moderationVerdictId)
                .orElseThrow(() -> new RuntimeException("Moderation verdict not found"));

        if (verdict.getApproved() != null) {
            throw new IllegalStateException("Verdict already decided");
        }

        User moderator = userService.findByEmail(user.getUsername());
        User recipient = null;
        String notificationText = null;
        String gameName = null;

        var gameVersion = verdict.getGameVersion();
        if (gameVersion != null) {
            recipient = gameVersion.getGame().getDeveloper();
            gameName = gameVersion.getGame().getName();
            notificationText = String.format(
                    "Ваша версия игры \"%s\" отклонена модератором @%s. Причина: %s",
                    gameName,
                    moderator.getUsername(),
                    reason);
        }

        var devApp = verdict.getDevApplication();
        if (devApp != null) {
            recipient = devApp.getUser();
            notificationText = String.format(
                    "Ваша заявка на статус разработчика отклонена модератором @%s. Причина: %s",
                    moderator.getUsername(),
                    reason);
        }

        var review = verdict.getReview();
        if (review != null) {
            recipient = review.getUser();
            gameName = review.getGame().getName();
            notificationText = String.format(
                    "Ваш отзыв на игру \"%s\" скрыт навсегда модератором @%s. Причина: %s",
                    gameName,
                    moderator.getUsername(),
                    reason);
        }

        verdict.setApproved(false);
        verdict.setReason(reason);
        verdict.setModerator(moderator);
        moderationVerdictRepository.save(verdict);

        if (recipient != null && notificationText != null) {
            createNotification(recipient, notificationText, Notification.Type.moderation);
        }
    }

    /**
     * Создает уведомление для пользователя.
     *
     * @param recipient получатель уведомления
     * @param text      текст уведомления
     * @param type      тип уведомления (system, warning, moderation, news)
     */
    private void createNotification(User recipient, String text, Notification.Type type) {
        Notification notification = new Notification();
        notification.setCreatedAt(Instant.now());
        notification.setType(type);
        notification.setText(text);

        notification.setUsers(new HashSet<>());
        notification.getUsers().add(recipient);

        notificationRepository.save(notification);
    }
}