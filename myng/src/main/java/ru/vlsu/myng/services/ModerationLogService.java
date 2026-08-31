package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.dto.ModerationItem;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для получения и отображения лога модерации.<br>
 * <br>
 * Обеспечивает формирование данных для журнала модерации:<br>
 * - получение полного списка решений модерации;<br>
 * - фильтрация по поисковому запросу, типу, статусу и периоду;<br>
 * - пагинация результатов для постраничного отображения;<br>
 * - преобразование сущностей {@link ModerationVerdict}
 *   в DTO {@link ModerationItem} для UI.<br>
 * <br>
 * Используется в следующих сценариях:<br>
 * - администратор просматривает все решения модераторов;<br>
 * - администратор фильтрует логи по типу (игры, заявки, отзывы);<br>
 * - администратор ищет конкретные решения по имени модератора или причине;<br>
 * - система отображает статистику модерации за выбранный период.<br>
 * <br>
 * Преобразование {@link ModerationVerdict} в {@link ModerationItem}
 * учитывает тип связанной сущности (версия игры, заявка разработчика, отзыв)
 * и заполняет соответствующие поля DTO.
 */
@Service
@RequiredArgsConstructor
public class ModerationLogService {

    private final ModerationVerdictRepository verdictRepository;

    /**
     * Возвращает полный список элементов модерации без фильтрации.
     *
     * <p>
     * Результаты сортируются по дате создания в обратном порядке
     * (сначала новые).
     * </p>
     *
     * @return список DTO элементов модерации. Никогда не возвращает null.
     *         Может быть пустым, если решения модерации отсутствуют.
     *
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    public List<ModerationItem> getModerationItems() {
        List<ModerationVerdict> verdicts = verdictRepository.findAll();

        return verdicts.stream()
                .map(this::toModerationItem)
                .sorted(Comparator.comparing(ModerationItem::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Возвращает страницу элементов модерации с фильтрацией и пагинацией.
     *
     * <p>
     * Поддерживаемые фильтры:
     * </p>
     * <ul>
     *   <li><b>search</b> — поиск по:
     *     <ul>
     *       <li>имени модератора;</li>
     *       <li>имени пользователя (для заявок);</li>
     *       <li>хешу коммита (для версий игр);</li>
     *       <li>причине решения;</li>
     *       <li>ID сущности.</li>
     *     </ul>
     *   </li>
     *   <li><b>type</b> — фильтрация по типу:
     *     <ul>
     *       <li>GAME_VERSION — версии игр;</li>
     *       <li>DEV_APPLICATION — заявки разработчиков;</li>
     *       <li>REVIEW — отзывы.</li>
     *     </ul>
     *   </li>
     *   <li><b>status</b> — фильтрация по статусу:
     *     <ul>
     *       <li>approved — одобренные;</li>
     *       <li>rejected — отклонённые;</li>
     *       <li>pending — ожидающие решения.</li>
     *     </ul>
     *   </li>
     *   <li><b>period</b> — фильтрация по периоду:
     *     <ul>
     *       <li>today — за последние 24 часа;</li>
     *       <li>week — за последние 7 дней;</li>
     *       <li>month — за последние 30 дней.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p>
     * Если параметр фильтра равен null или пустой строке,
     * соответствующая фильтрация не применяется.
     * </p>
     *
     * @param search   строка поискового запроса.
     *                 Может быть null или пустой строкой.
     * @param type     тип сущности для фильтрации.
     *                 Может быть null или пустой строкой.
     * @param status   статус решения для фильтрации.
     *                 Может быть null или пустой строкой.
     * @param period   временной период для фильтрации (today/week/month).
     *                 Может быть null.
     * @param pageable параметры пагинации. Не должен быть null.
     *
     * @return страница DTO элементов модерации,
     *         удовлетворяющих условиям фильтрации.
     *         Никогда не возвращает null.
     *
     * @throws IllegalArgumentException если pageable равен null
     * @throws org.springframework.dao.DataAccessException при ошибке доступа к БД
     */
    public Page<ModerationItem> getModerationItems(
            String search,
            String type,
            String status,
            String period,
            Pageable pageable ) {

        Instant createdAfter = switch (
                period == null ? "" : period
                ) {

            case "today" ->
                    Instant.now().minus(1, ChronoUnit.DAYS);

            case "week" ->
                    Instant.now().minus(7, ChronoUnit.DAYS);

            case "month" ->
                    Instant.now().minus(30, ChronoUnit.DAYS);

            default -> null;
        };
        
        return verdictRepository.getModerationItems(search, type, status, createdAfter, pageable);
    }

    /**
     * Преобразует сущность {@link ModerationVerdict} в DTO {@link ModerationItem}.
     *
     * <p>
     * В зависимости от типа связанной сущности заполняются
     * соответствующие поля DTO:
     * </p>
     * <ul>
     *   <li><b>GAME_VERSION</b> — заполняются поля:
     *     gameId, commitHash, changelog, repoUrl, createdAt;</li>
     *   <li><b>DEV_APPLICATION</b> — заполняются поля:
     *     username, githubLogin, description, createdAt;</li>
     *   <li><b>REVIEW</b> — заполняются поля:
     *     gameId, rating, reviewText, reportCount, createdAt.</li>
     * </ul>
     *
     * <p>
     * Общие поля (moderatorUsername, approved, reason) заполняются
     * независимо от типа сущности.
     * </p>
     *
     * @param verdict решение модерации. Не должно быть null.
     *
     * @return DTO с данными, специфичными для типа сущности.
     */
    private ModerationItem toModerationItem(ModerationVerdict verdict) {
        ModerationItem dto = new ModerationItem();

        // --- MODERATOR ---
        if (verdict.getModerator() != null) {
            dto.setModeratorUsername(verdict.getModerator().getUsername());
        }
        dto.setApproved(verdict.getApproved());
        dto.setReason(verdict.getReason());

        // --- GAME VERSION ---
        if (verdict.getGameVersion() != null) {
            var version = verdict.getGameVersion();
            dto.setId(version.getId());
            dto.setType("GAME_VERSION");
            dto.setGameId(version.getGame().getId());
            dto.setCommitHash(version.getCommitHash());
            dto.setChangelog(version.getChangelog());
            dto.setRepoUrl(version.getGame().getRepo());
            dto.setCreatedAt(version.getCreatedAt());
        }
        // --- DEV APPLICATION ---
        else if (verdict.getDevApplication() != null) {
            var app = verdict.getDevApplication();
            dto.setId(app.getId());
            dto.setType("DEV_APPLICATION");
            dto.setUsername(app.getUser().getUsername());
            dto.setGithubLogin(app.getGithubUsername());
            dto.setDescription(app.getText());
            dto.setCreatedAt(app.getCreatedAt());
        }
        // --- REVIEW ---
        else if (verdict.getReview() != null) {
            var review = verdict.getReview();
            dto.setId(review.getId());
            dto.setType("REVIEW");
            dto.setGameId(review.getGame().getId());
            dto.setRating(review.getRating() != null ? review.getRating().intValue() : null);
            dto.setReviewText(review.getText());
            dto.setReportCount(review.getReportCount());
            dto.setCreatedAt(review.getCreatedAt());
        }

        return dto;
    }
}