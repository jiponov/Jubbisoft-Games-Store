package app.game.model;

import app.user.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(nullable = false)
    private LocalDate releaseDate;

    private boolean isAvailable;

    private String imageCoverUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "games_users",
            joinColumns = @JoinColumn(name = "game_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    private List<User> purchasedByUsers;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", nullable = true)
    private User publisher;
}