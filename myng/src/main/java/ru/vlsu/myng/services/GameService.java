package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public List<Game> getDeveloperGames(Long userId) {
        User developer = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Developer not found"));

        return gameRepository.findByDeveloper(developer);
    }

    public Game getGameByRepo(String repo) {
        return gameRepository.findByRepo(repo)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    public List<Game> getGamesByGenre(Game.Genre genre) {
        return gameRepository.findByGenre(genre);
    }

    public boolean repoExists(String repo) {
        return gameRepository.existsByRepo(repo);
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }
}