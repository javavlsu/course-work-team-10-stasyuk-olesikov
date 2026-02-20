package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "dev_application",
        uniqueConstraints = @UniqueConstraint(columnNames = "fk_user"))
public class DevApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Instant createdAt;

    private String githubUsername;

    @Lob
    private String text;

    @OneToOne(optional = false)
    @JoinColumn(name = "fk_user")
    private User user;
}