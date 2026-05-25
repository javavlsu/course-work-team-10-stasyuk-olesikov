package ru.vlsu.myng.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class GithubConfig {

    @Bean
    public WebClient githubWebClient(
            WebClient.Builder builder,
            @Value("${github.api.url}")
            String githubApiUrl,
            @Value("${github.token}")
            String githubToken
    ) {

        HttpClient httpClient = HttpClient.create()
                .followRedirect(true);

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(githubApiUrl)
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
}