package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "game",
        uniqueConstraints = @UniqueConstraint(columnNames = "repo"))
public class Game
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String descr;

    @Column(nullable = false, unique = true)
    private String repo;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @ManyToOne
    @JoinColumn(name = "fk_dev")
    private User developer;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<GameVersion> versions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "game_tag",
            joinColumns = @JoinColumn(name = "fk_game"),
            inverseJoinColumns = @JoinColumn(name = "fk_tag")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(mappedBy = "games")
    private Set<Collection> collections = new HashSet<>();

    public enum Genre
    {
        action, adventure, rpg, simulation, strategy,
        sports, puzzle, horror, platformer,
        sandbox, visual_novel, roguelike
    }
}