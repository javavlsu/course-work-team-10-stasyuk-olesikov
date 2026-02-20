package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fk_user", "fk_game"}))
public class Review
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Short rating;

    @Lob
    private String text;

    private Instant createdAt;

    private Integer reportCount = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_game")
    private Game game;
}