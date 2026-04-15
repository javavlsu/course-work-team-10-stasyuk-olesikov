package ru.vlsu.myng.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.vlsu.myng.utils.GithubException;

import java.util.*;

// perhaps add caching later to avoid excess calls to GitHub API
@Service
public class GithubService {

    private final WebClient webClient;

    @Value("${github.token}")
    private String githubToken;

    public GithubService(WebClient.Builder builder) {
        this.webClient = builder
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


    public void validateFilesExistInCommit(String repoUrl, String commitHash, String files) {
        String[] path = extractPath(repoUrl).split("/");
        String owner = path[0];
        String repo = path[1];

        List<String> requestedFiles = Arrays.stream(files.split("\\s*,\\s*"))
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

            List<String> repoFiles = new ArrayList<>();

            for (JsonNode node : response.get("tree")) {
                if ("blob".equals(node.get("type").asText())) {
                    repoFiles.add(node.get("path").asText());
                }
            }

            System.out.println(repoFiles);

            if (!isValidFileMatch(requestedFiles, "index.html")) {
                throw new GithubException("files", "Список файлов должен содержать index.html");
            }

            List<String> missing = new ArrayList<>();

            for (String requested : requestedFiles) {

                boolean match = isValidFileMatch(repoFiles, requested);

                if (!match) {
                    missing.add(requested);
                }
            }

            if (!missing.isEmpty()) {
                throw new GithubException(
                        "files",
                        "Файлы не найдены в коммите: " + String.join(", ", missing)
                );
            }

        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException("files", "GitHub недоступен, попробуйте позже");
        }
    }

    private String extractPath(String url) {
        return url.replace("https://github.com/", "").replaceAll("/$", "");
    }

    private boolean isValidFileMatch(List<String> repoFiles, String requested) {

        List<String> matches = repoFiles.stream()
                .filter(path ->
                        path.equals(requested) ||
                                path.endsWith("/" + requested)
                )
                .toList();

        return matches.size() == 1;
    }
}
