package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.entities.Game;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.CollectionRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    public User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<Collection> getUserCollections(Integer userId) {
        User user = getUser(userId);
        return collectionRepository.findByUser(user);
    }

    public Collection getCollection(Integer collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
    }

    public List<Game> getCollectionGames(Integer collectionId) {
        return new ArrayList<>(getCollection(collectionId).getGames());
    }
}