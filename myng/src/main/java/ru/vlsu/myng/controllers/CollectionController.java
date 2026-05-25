package ru.vlsu.myng.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ru.vlsu.myng.dto.CreateCollectionDto;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.entities.Collection;
import ru.vlsu.myng.services.CollectionService;
import ru.vlsu.myng.services.UserService;
import ru.vlsu.myng.dto.CollectionName;

import java.security.Principal;

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
    public CreateCollectionDto createCollection(
            @ModelAttribute CreateCollectionDto dto) {

        Collection collection = collectionService.createCollection(dto.getUserId(), dto.getName());

        CreateCollectionDto res = new CreateCollectionDto();
        res.setUserId(collection.getId());
        res.setName(collection.getName());

        return res;
    }

    @PostMapping("/delete/{collectionId}")
    @ResponseBody
    public void deleteCollection(@PathVariable Integer collectionId) {
        collectionService.deleteCollection(collectionId);
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<?> editCollectionName(
            @PathVariable Integer id,
            @RequestBody CollectionName dto,
            Principal principal) {
        Collection collection = collectionService.findById(id);
        User currentUser = userService.findByEmail(principal.getName());

        if (!collection.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("Нельзя редактировать чужую коллекцию");
        }

        collection.setName(dto.getName());
        collectionService.save(collection);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{collectionId}/add-game/{gameId}")
    public ResponseEntity<?> addGame(@PathVariable Integer collectionId,
            @PathVariable Integer gameId,
            Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        try {
            collectionService.addGameToCollection(collectionId, gameId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{collectionId}/remove-game/{gameId}")
    @ResponseBody
    public void removeGameFromCollection(@PathVariable Integer collectionId,
                                         @PathVariable Integer gameId) {
        System.out.println("Removing gameid " + gameId + " from collectionid" + collectionId);
        collectionService.removeGameFromCollection(collectionId, gameId);
    }
}