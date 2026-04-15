package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.myng.entities.Tag;
import ru.vlsu.myng.repositories.TagRepository;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * Find tag by name or create if not exists.
     */
    @Transactional
    public Tag findOrCreate(String rawName) {
        String name = normalize(rawName);

        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    return tagRepository.save(tag);
                });
    }

    /**
     * Get tag by name (throws if not found).
     */
    public Tag getByName(String rawName) {
        String name = normalize(rawName);

        return tagRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + name));
    }

    /**
     * Parse DTO tag string:
     * "#action, #sci-fi" -> Set<Tag>
     */
    @Transactional
    public Set<Tag> parseTags(String tagsRaw) {
        if (tagsRaw == null || tagsRaw.isBlank()) {
            throw new IllegalArgumentException("Tags cannot be empty");
        }

        return Arrays.stream(tagsRaw.split(","))
                .map(String::trim)
                .map(tag -> tag.startsWith("#") ? tag.substring(1) : tag)
                .map(this::normalize)
                .map(this::findOrCreate)
                .collect(Collectors.toSet());
    }

    /**
     * Normalize tag:
     * - lowercase
     * - trim
     * - replace spaces with "-"
     */
    private String normalize(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }

        return tag.trim()
                .toLowerCase()
                .replace(" ", "-");
    }
}