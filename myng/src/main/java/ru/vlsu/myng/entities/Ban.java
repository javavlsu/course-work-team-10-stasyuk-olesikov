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
@Table(name = "ban")
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_mod")
    private User moderator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user")
    private User user;
}