package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "game_version", uniqueConstraints = @UniqueConstraint(columnNames = { "fk_game", "commit_hash" }))
public class GameVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Instant createdAt;

    @Column(nullable = false)
    private String commitHash;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] archiveFile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_game")
    private Game game;

    @OneToOne(mappedBy = "gameVersion")
    private ModerationVerdict moderationVerdict;
}