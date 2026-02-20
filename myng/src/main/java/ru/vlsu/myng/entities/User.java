package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "user",
        uniqueConstraints =
        {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "github_username")
        })
public class User
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String username;

    @Column(length = 255)
    private String bio;

    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] profilePic;

    private Instant registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.user;

    private String githubUsername;

    @OneToMany(mappedBy = "developer")
    private List<Game> games = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    public enum Role
    {
        user, dev, mod, admin
    }
}