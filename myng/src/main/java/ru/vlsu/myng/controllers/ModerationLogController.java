package  ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ModerationLogController {

    private final ModerationVerdictRepository verdictRepository;

    @GetMapping("/moderation-log")
    public String moderationPage(Model model) {

        List<ModerationVerdict> verdicts = verdictRepository.findAll();
        List<ModerationItem> items = new ArrayList<>();

        for (ModerationVerdict verdict : verdicts) {

            ModerationItem dto = new ModerationItem();

            // --- GAME VERSION ---
            if (verdict.getGameVersion() != null) {

                var version = verdict.getGameVersion();

                dto.setId(version.getId().longValue());
                dto.setType("GAME_VERSION");
                dto.setGameId(version.getGame().getId().longValue());
                dto.setCommitHash(version.getCommitHash());
                dto.setChangelog(version.getChangelog());
                dto.setRepoUrl(version.getGame().getRepo());
                dto.setCreatedAt(version.getCreatedAt());

            }
            // --- DEV APPLICATION ---
            else if (verdict.getDevApplication() != null) {

                var app = verdict.getDevApplication();

                dto.setId(app.getId().longValue());
                dto.setType("DEV_APPLICATION");
                dto.setUsername(app.getUser().getUsername());
                dto.setGithubLogin(app.getGithubUsername());
                dto.setDescription(app.getText());
                dto.setCreatedAt(app.getCreatedAt());

            }
            // --- REVIEW ---
            else if (verdict.getReview() != null) {

                var review = verdict.getReview();

                dto.setId(review.getId().longValue());
                dto.setType("REVIEW");
                dto.setGameId(review.getGame().getId().longValue());
                dto.setRating(review.getRating() != null ? review.getRating().intValue() : null);
                dto.setReviewText(review.getText());
                dto.setReportCount(review.getReportCount());
                dto.setCreatedAt(review.getCreatedAt());
            }

            items.add(dto);
        }

        // Сортировка по дате (новые сверху)
        items.sort(Comparator.comparing(ModerationItem::getCreatedAt).reversed());

        model.addAttribute("moderationItems", items);

        return "moderation_log"

}