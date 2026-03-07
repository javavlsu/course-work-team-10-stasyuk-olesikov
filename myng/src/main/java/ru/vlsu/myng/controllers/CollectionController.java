package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.services.CollectionService;
import ru.vlsu.myng.services.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final UserService userService;

    @GetMapping("/user/{userId}")
    public String userCollections(@PathVariable Integer userId, Model model) {

        model.addAttribute("user", userService.findById(userId));
        model.addAttribute("collections", collectionService.getUserCollections(userId));

        return "fragments/collections :: collectionsFragment";
    }

    @GetMapping("/{collectionId}")
    public String collectionDetails(@PathVariable Integer collectionId, Model model) {

        Collection collection = collectionService.getCollection(collectionId);

        model.addAttribute("collection", collection);
        model.addAttribute("games", collectionService.getCollectionGames(collectionId));

        return "fragments/collection_games :: gamesFragment";
    }

    @PostMapping("/create")
    @ResponseBody
    public Collection createCollection(
            @RequestParam String collection_name,
            @RequestParam Integer userId
    ) {

        return collectionService.createCollection(userId, collection_name);
    }

    @PostMapping("/delete/{collectionId}")
    @ResponseBody
    public void deleteCollection(@PathVariable Integer collectionId) {
        collectionService.deleteCollection(collectionId);
    }
}