package app.loyalty.model;

import app.transaction.model.*;
import app.user.model.*;
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
public class Loyalty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoyaltyType type;

    @Column(nullable = false)
    private int gamesPurchased;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id", nullable = false)
    private User member;
}