package app.treasury.model;

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
public class Treasury {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // единственото Treasury в ПРОЕКТА: "Treasury vault"
    // private String name = "Treasury vault";
    @Column(nullable = false, unique = true)
    private String name;

    // ще го заредя предварително със сума
    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}