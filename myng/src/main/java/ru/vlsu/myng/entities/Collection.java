package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "collection",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "fk_user"}))
public class Collection
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "game_collection",
            joinColumns = @JoinColumn(name = "fk_collection"),
            inverseJoinColumns = @JoinColumn(name = "fk_game")
    )
    private Set<Game> games = new HashSet<>();
}