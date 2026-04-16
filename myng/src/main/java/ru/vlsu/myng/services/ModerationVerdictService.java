package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationVerdictService {
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final GithubService githubService;

    public ModerationVerdict save(ru.vlsu.myng.entities.ModerationVerdict verdict) { return moderationVerdictRepository.save(verdict); }

    public Optional<ModerationVerdict> findByDevApplication(DevApplication app) { return moderationVerdictRepository.findByDevApplication(app); }

    public void approve(Integer moderationVerdictId) {
        ModerationVerdict verdict = moderationVerdictRepository.findById(moderationVerdictId)
                .orElseThrow(() -> new RuntimeException("Moderation verdict not found"));

        if (verdict.getApproved() != null) {
            throw new IllegalStateException("Verdict already decided");
        }

        verdict.setApproved(true);

        var gv = verdict.getGameVersion();
        if (gv != null) {
            githubService.downloadGameVersion(gv);
        }

        moderationVerdictRepository.save(verdict);
    }

    public void reject(Integer moderationVerdictId, String reason) {
        ModerationVerdict verdict = moderationVerdictRepository.findById(moderationVerdictId)
                .orElseThrow(() -> new RuntimeException("Moderation verdict not found"));

        if (verdict.getApproved() != null) {
            throw new IllegalStateException("Verdict already decided");
        }

        verdict.setApproved(false);
        verdict.setReason(reason);

        moderationVerdictRepository.save(verdict);
    }
}