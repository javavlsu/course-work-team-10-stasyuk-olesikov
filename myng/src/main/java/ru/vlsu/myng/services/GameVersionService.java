package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.myng.dto.PublishGameVersionRequest;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.GameVersionRepository;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GameVersionService {

    private final GameVersionRepository gameVersionRepository;
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final GameService gameService;
    private final GithubService githubService;

    @Value("${app.storage.path}")
    private String storagePath;

    @Transactional(readOnly = true)
    public GameVersion getGameVersionById(Integer id) {
        return gameVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Версия игры не найдена"));
    }

    public String resolveEntryPoint(GameVersion version) {

        Path baseDir = Paths.get(storagePath,
                "gamefiles",
                "game_" + version.getGame().getId(),
                "ver_" + version.getId());

        try (Stream<Path> walk = Files.walk(baseDir)) {

            Path indexFile = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("index.html"))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("Entry file not found in " + baseDir));

            // Convert filesystem path to web path
            Path storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
            Path fullPath = indexFile.toAbsolutePath().normalize();

            Path relative = storageRoot.relativize(fullPath);

            return "http://localhost:8080/static/" + relative.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve entry point for version " + version.getId(), e);
        }
    }

    public void publishGameVersion(PublishGameVersionRequest dto) {
        var game = gameService.getGameById(dto.getGameId());

        githubService.validateCommitExists(game.getRepo(), dto.getCommitHash());
        githubService.validateFilesExistInCommit(game.getRepo(), dto.getCommitHash(), dto.getFiles());

        GameVersion version = new GameVersion();
        version.setGame(game);
        version.setCommitHash(dto.getCommitHash());
        version.setName(dto.getGameVerName());
        version.setCreatedAt(Instant.now());
        version.setFiles(dto.getFiles());
        version.setChangelog(dto.getChangelog());

        gameVersionRepository.save(version);

        ModerationVerdict verdict = new ModerationVerdict();
        verdict.setGameVersion(version);
        verdict.setApproved(null);
        verdict.setReason(null);
        verdict.setModerator(null);

        moderationVerdictRepository.save(verdict);

        version.setModerationVerdict(verdict);
    }
}