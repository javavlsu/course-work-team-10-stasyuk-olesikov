package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "warning")
public class Warning
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String reason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_mod")
    private User moderator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user")
    private User user;
}