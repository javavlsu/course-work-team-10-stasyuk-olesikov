package ru.vlsu.myng.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.vlsu.myng.dto.PublishGameVersionRequest;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.entities.ModerationVerdict;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.repositories.GameVersionRepository;
import ru.vlsu.myng.repositories.ModerationVerdictRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class GameVersionServiceTest {

    @Mock
    private GameVersionRepository gameVersionRepository;

    @Mock
    private ModerationVerdictRepository moderationVerdictRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameService gameService;

    @Mock
    private GithubService githubService;

    @InjectMocks
    private GameVersionService gameVersionService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                gameVersionService,
                "storagePath",
                tempDir.toString()
        );

        ReflectionTestUtils.setField(
                gameVersionService,
                "port",
                "8080"
        );
    }

    @Test
    void shouldResolveEntryPoint() throws IOException {

        Game game = new Game();
        game.setId(7);

        GameVersion version = new GameVersion();
        version.setId(14);
        version.setGame(game);

        Path versionDir = tempDir
                .resolve("gamefiles")
                .resolve("game_7")
                .resolve("ver_14");

        Files.createDirectories(versionDir);

        Path indexFile = versionDir.resolve("index.html");

        Files.writeString(indexFile, "<html>Hello</html>");

        String result = gameVersionService.resolveEntryPoint(version);

        assertEquals(
                "http://localhost:8080/static/gamefiles/game_7/ver_14/index.html",
                result
        );
    }

    @Test
    void shouldThrow_WhenEntryPointMissing() throws IOException
    {
        Game game = new Game();
        game.setId(1);

        GameVersion version = new GameVersion();
        version.setId(2);
        version.setGame(game);

        Path versionDir = tempDir
                .resolve("gamefiles")
                .resolve("game_1")
                .resolve("ver_2");

        Files.createDirectories(versionDir);
        
        Files.writeString(
                versionDir.resolve("README.txt"),
                "no index here"
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> gameVersionService.resolveEntryPoint(version)
        );

        assertTrue(
                ex.getMessage().contains("Entry file not found")
        );
    }

    @Test
    void shouldPublishGameVersion() {

        Game game = new Game();
        game.setId(10);
        game.setRepo("https://github.com/test/repo");

        PublishGameVersionRequest dto = new PublishGameVersionRequest();
        dto.setGameId(10);
        dto.setCommitHash("abc123");
        dto.setFiles("index.html");
        dto.setGameVerName("v1.0");
        dto.setChangelog("Initial release");

        when(gameService.getGameById(10)).thenReturn(game);

        when(gameVersionRepository.save(any(GameVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(moderationVerdictRepository.save(any(ModerationVerdict.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        gameVersionService.publishGameVersion(dto);

        verify(githubService).validateCommitExists(
                game.getRepo(),
                "abc123"
        );

        verify(githubService).validateFilesExistInCommit(
                game.getRepo(),
                "abc123",
                "index.html"
        );

        verify(gameVersionRepository).save(any(GameVersion.class));

        verify(moderationVerdictRepository)
                .save(any(ModerationVerdict.class));
    }

    @Test
    void shouldDeleteGameVersion() {

        Game game = new Game();
        game.setId(7);

        ModerationVerdict verdict = new ModerationVerdict();

        GameVersion version = new GameVersion();
        version.setId(14);
        version.setGame(game);
        version.setModerationVerdict(verdict);

        when(gameVersionRepository.findById(14))
                .thenReturn(Optional.of(version));

        when(gameVersionRepository.findByGameIdOrderByCreatedAtAsc(7))
                .thenReturn(List.of());

        gameVersionService.deleteGameVersion(7, 14);

        verify(moderationVerdictRepository).delete(verdict);

        verify(gameVersionRepository).delete(version);

        verify(gameRepository).save(game);

        assertNull(game.getFirstReleaseDate());
    }

    @Test
    void shouldThrow_WhenDeletingVersionFromWrongGame() {

        Game game = new Game();
        game.setId(999);

        GameVersion version = new GameVersion();
        version.setId(14);
        version.setGame(game);

        when(gameVersionRepository.findById(14))
                .thenReturn(Optional.of(version));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> gameVersionService.deleteGameVersion(7, 14)
        );

        assertEquals(
                "Версия не принадлежит данной игре",
                ex.getMessage()
        );
    }

    @Test
    void shouldUpdateFirstReleaseDateAfterDeletion() {

        Game game = new Game();
        game.setId(7);

        GameVersion deletedVersion = new GameVersion();
        deletedVersion.setId(14);
        deletedVersion.setGame(game);

        GameVersion remainingVersion = new GameVersion();
        remainingVersion.setCreatedAt(
                Instant.parse("2024-01-01T00:00:00Z")
        );

        when(gameVersionRepository.findById(14))
                .thenReturn(Optional.of(deletedVersion));

        when(gameVersionRepository.findByGameIdOrderByCreatedAtAsc(7))
                .thenReturn(List.of(remainingVersion));

        gameVersionService.deleteGameVersion(7, 14);

        assertEquals(
                Instant.parse("2024-01-01T00:00:00Z"),
                game.getFirstReleaseDate()
        );

        verify(gameRepository).save(game);
    }
}