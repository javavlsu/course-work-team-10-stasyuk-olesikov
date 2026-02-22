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
@Table(name = "review", uniqueConstraints = @UniqueConstraint(columnNames = { "fk_user", "fk_game" }))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Byte rating;

    @Column(columnDefinition = "TEXT")
    private String text;

    private Instant createdAt;

    @Column(nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer reportCount = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_game")
    private Game game;
}