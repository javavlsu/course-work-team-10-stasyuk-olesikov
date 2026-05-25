package ru.vlsu.myng.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.GameVersion;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock
class GithubServiceTest {

    @Autowired
    private GithubService githubService;

    @InjectWireMock
    private WireMockServer wireMockServer;

    @TempDir
    Path tempStorageDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.token", () -> "mock-test-token");
        registry.add("app.storage.path", () -> System.getProperty("java.io.tmpdir"));
    }

    @Test
    void should_DownloadZip_ExtractFiles_AndInjectScriptsIntoHtml() throws Exception {
//        WebClient testWebClient = WebClient.builder()
//                .baseUrl(wireMockServer.baseUrl())
//                .build();
//        ReflectionTestUtils.setField(githubService, "webClient", testWebClient);
        
        ReflectionTestUtils.setField(githubService, "storagePath", tempStorageDir.toString());
        
        Game game = new Game();
        game.setId(7);
        game.setRepo("https://github.com/my-owner/my-repo");

        GameVersion version = new GameVersion();
        version.setId(14);
        version.setCommitHash("xyz987");
        version.setGame(game);
        version.setFiles("index.html");

        // Programmatically generate a dummy ZIP in memory (Mimicking GitHub's layout)
        // GitHub always wraps repo contents inside a root folder (e.g., 'repo-name-commithash/')
        byte[] mockZipBytes = createMockGithubZip("my-repo-xyz987/index.html", "<html><head></head><body>Hello World</body></html>");

        // Stub the WireMock server endpoint
        wireMockServer.stubFor(get(urlEqualTo("/repos/my-owner/my-repo/zipball/xyz987"))
                .withHeader("Authorization", containing("Bearer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/zip")
                        .withBody(mockZipBytes)));
        
        githubService.downloadGameVersion(version);
        
        Path expectedTargetDir = tempStorageDir.resolve("gamefiles")
                .resolve("game_7")
                .resolve("ver_14");

        Path expectedHtmlFile = expectedTargetDir.resolve("index.html");

        assertTrue(Files.exists(expectedHtmlFile), "index.html should be extracted to the target directory");

        String finalHtml = Files.readString(expectedHtmlFile);

        assertTrue(finalHtml.contains("<script>window.__GAME_ID__=7; window.__GAMEVER_ID__=14</script>"),
                "The game IDs should be injected into the window context variables.");

        assertTrue(finalHtml.contains("<script src=\"/__myng-storage-patch.js\"></script>"),
                "The patch script inclusion reference tag should be present.");
    }

    /**
     * Helper utility to create a valid zip stream containing a mock file layout.
     */
    private byte[] createMockGithubZip(String filePath, String content) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(filePath);
            zos.putNextEntry(entry);
            zos.write(content.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}