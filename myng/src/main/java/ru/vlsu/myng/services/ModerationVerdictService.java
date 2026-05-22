package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.User.Role;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationVerdictService {
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final GithubService githubService;
    private final UserService userService;
    private final UserRepository userRepository;

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

        var gv = verdict.getGameVersion();
        if (gv != null) {
            githubService.downloadGameVersion(gv);
        }

        var devApp = verdict.getDevApplication();
        if (devApp != null) {
            User applicant = devApp.getUser();

            applicant.setRole(Role.dev);

            userRepository.save(applicant);
        }

        var review = verdict.getReview();
        if (review != null) {
            review.setReportCount(0);
        }

        ru.vlsu.myng.entities.User mod = userService.findByEmail(user.getUsername());

        verdict.setApproved(true);
        verdict.setModerator(mod);

        moderationVerdictRepository.save(verdict);
    }

    @Transactional
    public void reject(Integer moderationVerdictId, String reason,
            org.springframework.security.core.userdetails.User user) {
        ModerationVerdict verdict = moderationVerdictRepository.findById(moderationVerdictId)
                .orElseThrow(() -> new RuntimeException("Moderation verdict not found"));

        if (verdict.getApproved() != null) {
            throw new IllegalStateException("Verdict already decided");
        }

        User mod = userService.findByEmail(user.getUsername());

        verdict.setApproved(false);
        verdict.setReason(reason);
        verdict.setModerator(mod);

        moderationVerdictRepository.save(verdict);
    }
}