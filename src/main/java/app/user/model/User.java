package app.user.model;

import app.game.model.*;
import app.wallet.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

    private boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id", unique = true)
    private Wallet wallet;

    @ManyToMany(mappedBy = "purchasedByUsers", fetch = FetchType.EAGER)
    private List<Game> boughtGames = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "publisher")
    @OrderBy("releaseDate DESC")
    private List<Game> createdGames = new ArrayList<>();
}