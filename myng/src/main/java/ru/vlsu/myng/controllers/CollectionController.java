package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.CollectionRepository;
import ru.vlsu.myng.repositories.UserRepository;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    /**
     * Show all collections for a specific user
     */
    @GetMapping("/user/{userId}")
    public String userCollections(@PathVariable Integer userId, Model model) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<Collection> collections =
                collectionRepository.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("collections", collections);

        return "fragments/collections :: collectionsFragment";
    }

    /**
     * Show games inside a collection
     */
    @GetMapping("/{collectionId}")
    public String collectionDetails(@PathVariable Integer collectionId,
                                    Model model) {

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Collection not found"));

        model.addAttribute("collection", collection);
        model.addAttribute("games", collection.getGames());

        return "fragments/collection_games :: gamesFragment";
    }
}