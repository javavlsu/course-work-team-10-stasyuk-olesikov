package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "game_version",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fk_game", "commit_hash"}))
public class GameVersion
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Instant createdAt;

    @Column(nullable = false)
    private String commitHash;

    @Lob
    private String changelog;

    @Column(nullable = false, length = 20)
    private String name;

    @Lob
    @Column(nullable = false)
    private byte[] archiveFile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_game")
    private Game game;
}