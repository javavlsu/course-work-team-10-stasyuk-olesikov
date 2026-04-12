package ru.vlsu.myng.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.vlsu.myng.utils.GithubException;

// perhaps add caching later to avoid excess calls to GitHub API
@Service
public class GithubService {

    private final WebClient webClient;

    public GithubService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("User-Agent", "MYNG")
                .build();
    }

    public void validateRepoExists(String repoUrl) {
        String path = extractPath(repoUrl);

        try {
            webClient.get()
                    .uri("/repos/{ownerRepo}", path)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND || status == HttpStatus.BAD_REQUEST,
                            response -> {
                                throw new GithubException("repoLink", "Репозиторий не найден");
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> {
                                throw new GithubException("repoLink", "Ошибка при проверке репозитория");
                            }
                    )
                    .toBodilessEntity()
                    .block(); // blocking ON PURPOSE (we're in MVC)

        } catch (GithubException e) {
            throw e;
        } catch (Exception e) {
            throw new GithubException("repoLink", "GitHub недоступен, попробуйте позже");
        }
    }

    public void validateCommitExists(String repoUrl, String commitHash) {
        String path = extractPath(repoUrl);

        try {
            webClient.get()
                    .uri("/repos/{ownerRepo}/commits/{commit}", path, commitHash)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND || status == HttpStatus.BAD_REQUEST,
                            response -> {
                                throw new GithubException("commitHash", "Коммит не найден в данном репозитории или не принадлежит ему");
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> {
                                throw new GithubException("commitHash", "Ошибка при проверке коммита");
                            }
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
}
