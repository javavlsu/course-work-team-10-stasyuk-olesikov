package ru.vlsu.myng.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "moderation_verdict")
public class ModerationVerdict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Boolean approved;

    private String reason;

    // проверка того, что лишь один из внешних ключей не null нужно проверять на
    // уровне бизнес-логики

    @OneToOne
    @JoinColumn(name = "fk_game_version")
    private GameVersion gameVersion;

    @OneToOne
    @JoinColumn(name = "fk_dev_application")
    private DevApplication devApplication;

    @OneToOne
    @JoinColumn(name = "fk_review")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "fk_mod")
    private User moderator;
}