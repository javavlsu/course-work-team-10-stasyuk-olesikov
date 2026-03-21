package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "game", uniqueConstraints = @UniqueConstraint(columnNames = "repo"))
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
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
    @JoinTable(name = "game_tag", joinColumns = @JoinColumn(name = "fk_game"), inverseJoinColumns = @JoinColumn(name = "fk_tag"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(mappedBy = "games")
    private Set<Collection> collections = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    public enum Genre {
        action, adventure, rpg, simulation, strategy,
        sports, puzzle, horror, platformer,
        sandbox, visual_novel, roguelike
    }
}