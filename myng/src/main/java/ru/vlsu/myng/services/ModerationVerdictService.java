package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationVerdictService {
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final GithubService githubService;
    private final UserService userService;

    public ModerationVerdict save(ru.vlsu.myng.entities.ModerationVerdict verdict) { return moderationVerdictRepository.save(verdict); }

    public Optional<ModerationVerdict> findByDevApplication(DevApplication app) { return moderationVerdictRepository.findByDevApplication(app); }

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

        User mod = userService.findByEmail(user.getUsername());

        verdict.setApproved(true);
        verdict.setModerator(mod);

        moderationVerdictRepository.save(verdict);
    }

    public void reject(Integer moderationVerdictId, String reason, org.springframework.security.core.userdetails.User user) {
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