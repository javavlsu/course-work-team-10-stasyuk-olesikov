package ru.vlsu.myng.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.vlsu.myng.entities.GameVersion;
import ru.vlsu.myng.utils.GithubException;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// perhaps add caching later to avoid excess calls to GitHub API
@Service
public class GithubService {

    private final WebClient webClient;

    @Value("${github.token}")
    private String githubToken;

    @Value("${app.storage.path}")
    private String storagePath;

    public GithubService(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true);
        this.webClient = builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .defaultHeader(HttpHeaders.USER_AGENT, "MyNG")
                .filter((request, next) -> {
                    String token = githubToken;
                    ClientRequest newRequest = ClientRequest.from(request)
                            .headers(h -> h.setBearerAuth(token))
                            .build();
                    return next.exchange(newRequest);
                })
                .build();
    }

    public void validateRepoExists(String repoUrl) {
        String[] parts = extractPath(repoUrl).split("/");
        System.out.println("GITHUB TOKEN: " + githubToken);
        System.out.println(Arrays.toString(parts));

        String owner = parts[0];
        String repo = parts[1];

        try {
            webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND || status == HttpStatus.BAD_REQUEST,
                            response -> Mono.error(new GithubException("repoLink", "Репозиторий не найден"))
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class).flatMap(body -> {
                                System.out.println("GitHub error response body: " + body);
                                System.out.println("Status code: " + response.statusCode());

                                return Mono.error(new GithubException(
                                        "repoLink",
                                        "GitHub error: " + response.statusCode()
                                ));
                            })
                    )
                    .toBodilessEntity()
                    .block(); // blocking ON PURPOSE (we're in MVC)

        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new GithubException("repoLink", "GitHub недоступен, попробуйте позже");
        }
    }

    public void validateCommitExists(String repoUrl, String commitHash) {
        String[] path = extractPath(repoUrl).split("/");

        try {
            webClient.get()
                    .uri("/repos/{owner}/{repo}/commits/{commit}",
                            path[0],
                            path[1],
                            commitHash)
                    .retrieve()

                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            response -> Mono.error(new GithubException(
                                    "commitHash",
                                    "Коммит не найден в данном репозитории или не принадлежит ему"
                            ))
                    )

                    .onStatus(
                            HttpStatusCode::isError,
                            response -> Mono.error(new GithubException(
                                    "commitHash",
                                    "Ошибка при проверке коммита"
                            ))
                    )

                    .toBodilessEntity()
                    .block();

        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException("commitHash", "GitHub недоступен, попробуйте позже");
        }
    }

    private String extractPath(String url) {
        return url.replace("https://github.com/", "").replaceAll("/$", "");
    }

    public void validateFilesExistInCommit(String repoUrl, String commitHash, String files) {
        String[] path = extractPath(repoUrl).split("/");
        String owner = path[0];
        String repo = path[1];

        List<String> requestedPaths = Arrays.stream(files.split("\\s*,\\s*"))
                .map(String::trim)
                .toList();

        try {
            JsonNode response = webClient.get()
                    .uri("/repos/{owner}/{repo}/git/trees/{sha}?recursive=1",
                            owner,
                            repo,
                            commitHash)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            res -> Mono.error(new GithubException(
                                    "files",
                                    "Ошибка при получении файлов коммита"
                            ))
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("tree") == null) {
                throw new GithubException("files", "Не удалось получить дерево файлов коммита");
            }

            List<String> repoPaths = new ArrayList<>();
            for (JsonNode node : response.get("tree")) {
                String type = node.get("type").asText();
                String pathValue = node.get("path").asText();

                if ("blob".equals(type) || "tree".equals(type)) {
                    repoPaths.add(pathValue);
                }
            }

            boolean hasIndexHtml = requestedPaths.stream()
                    .map(p -> p.replaceAll("/+$", "")) // normalize trailing slash
                    .anyMatch(requested ->
                            repoPaths.stream().anyMatch(repoPath ->
                                    repoPath.equals(requested + "/index.html")
                                            || repoPath.equals("index.html")
                                            || repoPath.equals(requested)
                            )
                    );

            if (!hasIndexHtml) {
                throw new GithubException(
                        "files",
                        "Список должен содержать index.html (в корне или в папке)"
                );
            }

            List<String> missing = new ArrayList<>();

            for (String requested : requestedPaths) {
                if (!existsInRepo(repoPaths, requested)) {
                    missing.add(requested);
                }
            }

            if (!missing.isEmpty()) {
                throw new GithubException(
                        "files",
                        "Файлы/папки не найдены в коммите: " + String.join(", ", missing)
                );
            }

        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException("files", "GitHub недоступен, попробуйте позже");
        }
    }

    private boolean existsInRepo(List<String> repoPaths, String requested) {

        String normalized = requested.endsWith("/")
                ? requested.substring(0, requested.length() - 1)
                : requested;

        boolean exactMatch = repoPaths.stream()
                .anyMatch(path ->
                        path.equals(normalized) ||
                                path.endsWith("/" + normalized)
                );

        if (exactMatch) {
            return true;
        }

        return repoPaths.stream()
                .anyMatch(path ->
                        path.startsWith(normalized + "/") ||
                                path.contains("/" + normalized + "/")
                );
    }

    public void downloadGameVersion(GameVersion version) {
        String repoUrl = version.getGame().getRepo();
        String commit = version.getCommitHash();

        String[] path = extractPath(repoUrl).split("/");
        String owner = path[0];
        String repo = path[1];

        Path targetDir = buildTargetPath(version);

        System.out.println("Game download target directory: " + targetDir);

        try {
            Files.createDirectories(targetDir);

            Path zipFile = downloadZip(owner, repo, commit);
            extractNeededFiles(zipFile, targetDir, version.getFiles(), version);

            Files.deleteIfExists(zipFile);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки файлов из GitHub", e);
        }
    }

    private Path buildTargetPath(GameVersion version) {
        return Paths.get(storagePath,
                "gamefiles",
                "game_" + version.getGame().getId(),
                "ver_" + version.getId());
    }

    private Path downloadZip(String owner, String repo, String commit) throws IOException
    {
        String url = String.format(
                "https://api.github.com/repos/%s/%s/zipball/%s",
                owner, repo, commit
        );

        Path tempFile = Files.createTempFile("repo-", ".zip");

        Flux<DataBuffer> flux = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        DataBufferUtils.write(flux, tempFile)
                .block();

        return tempFile;
    }

    private void extractNeededFiles(Path zipPath, Path targetDir, String files, GameVersion version) throws IOException {
        List<String> requested = Arrays.stream(files.split("\\s*,\\s*"))
                .map(s -> s.replaceAll("^/+", "").replaceAll("/+$", ""))
                .toList();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                String fullPath = entry.getName();

                // remove root folder
                String relativePath = fullPath.substring(fullPath.indexOf("/") + 1);

                if (relativePath.isBlank()) continue;

                boolean shouldExtract = requested.stream()
                        .anyMatch(req ->
                                relativePath.equals(req) ||
                                        relativePath.startsWith(req + "/")
                        );

                if (!shouldExtract) continue;

                Path normalizedTargetDir = targetDir.normalize();
                Path normalized = normalizedTargetDir.resolve(relativePath).normalize();

                if (!normalized.startsWith(normalizedTargetDir)) {
                    throw new RuntimeException("Invalid path in zip: " + relativePath);
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(normalized);
                } else {
                    Files.createDirectories(normalized.getParent());
                    if (relativePath.endsWith("index.html")) {

                        String html = new String(zis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

                        html = injectIntoHead(html, buildPatch(version));

                        Files.writeString(normalized, html, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    } else {
                        Files.copy(zis, normalized, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private String injectIntoHead(String html, String injection) {
        if (html == null) return null;

        if (html.contains("__myng-storage-patch.js")) {
            return html;
        }

        return html.replaceFirst("(?i)<head>", "<head>\n" + injection);
    }

    private String buildPatch(GameVersion version) {
        return "<script>window.__GAME_ID__=" + version.getGame().getId() + "; window.__GAMEVER_ID__=" + version.getId() + "</script>\n"
                + "<script src=\"/__myng-storage-patch.js\"></script>";
    }
}