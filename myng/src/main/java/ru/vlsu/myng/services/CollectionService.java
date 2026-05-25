package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.vlsu.myng.dto.CollectionDTO;
import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.CollectionRepository;
import ru.vlsu.myng.repositories.GameRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public List<Collection> getUserCollections(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ;
        return collectionRepository.findByUser(user);
    }

    public Collection getCollection(Integer collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
    }

    public List<Game> getCollectionGames(Integer collectionId) {
        return new ArrayList<>(getCollection(collectionId).getGames());
    }

    public Collection createCollection(Integer userId, String name) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Collection collection = new Collection();
        collection.setName(name);
        collection.setUser(user);

        return collectionRepository.save(collection);
    }

    public void deleteCollection(Integer collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        collectionRepository.delete(collection);
    }

    @Transactional
    public Collection save(Collection collection) {
        return collectionRepository.save(collection);
    }

    public Collection findById(Integer id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found: " + id));
    }

    public List<CollectionDTO> findAllByUserGameNotIn(Integer userId, Integer gameId) {
        return collectionRepository.findAllByUserIdAndGameNotPresent(userId, gameId)
                .stream()
                .map(c -> new CollectionDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addGameToCollection(Integer collectionId, Integer gameId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Коллекция не найдена"));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Игра не найдена"));

        collection.getGames().add(game);
        collectionRepository.save(collection);
    }

    @Transactional
    public void removeGameFromCollection(Integer collectionId, Integer gameId) {

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Коллекция не найдена"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Игра не найдена"));

        collection.getGames().removeIf(g -> g.getId().equals(gameId));

        collectionRepository.save(collection);
    }
}