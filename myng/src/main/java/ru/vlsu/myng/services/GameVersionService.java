package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.myng.dto.PublishGameVersionRequest;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.repositories.GameVersionRepository;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.stream.Stream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class GameVersionService {

    private final GameVersionRepository gameVersionRepository;
    private final ModerationVerdictRepository moderationVerdictRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final GithubService githubService;

    @Value("${app.storage.path}")
    private String storagePath;

    @Value("${server.port}")
    private String port;

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
                    .orElseThrow(() -> new RuntimeException("Entry file not found in " + baseDir));

            // Convert filesystem path to web path
            Path storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
            Path fullPath = indexFile.toAbsolutePath().normalize();

            Path relative = storageRoot.relativize(fullPath);

            return "http://localhost:" + port + "/static/" + relative.toString().replace("\\", "/");
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

    @Transactional
    public void deleteGameVersion(Integer gameId, Integer versionId) {
        GameVersion version = gameVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Версия не найдена"));

        if (!version.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Версия не принадлежит данной игре");
        }

        Path versionPath = Paths.get("..", "storage", "gamefiles", "game_" + gameId, "ver_" + versionId);

        if (Files.exists(versionPath)) {
            try {
                Files.walk(versionPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                System.out.println("Папка с файлами версии успешно удалена: " + versionPath.toAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Не удалось удалить файлы версии с сервера: " + e.getMessage());
            }
        }
        
        if (version.getModerationVerdict() != null) {
            moderationVerdictRepository.delete(version.getModerationVerdict());
        }
        gameVersionRepository.delete(version);

        Game game = version.getGame();

        var remainingVersions = gameVersionRepository.findByGameIdOrderByCreatedAtAsc(gameId);

        if (remainingVersions.isEmpty()) {
            game.setFirstReleaseDate(null);
        } else {
            Instant oldestVersionDate = remainingVersions.get(0).getCreatedAt();
            game.setFirstReleaseDate(oldestVersionDate);
        }

        gameRepository.save(game);
    }
}