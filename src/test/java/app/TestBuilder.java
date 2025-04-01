package app;

import app.game.model.Game;
import app.loyalty.model.*;
import app.user.model.*;
import app.wallet.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@UtilityClass
public class TestBuilder {
    // API tests

    public static User aRandomUser() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .username("testUser")
                .password("securePass123")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .profilePicture(null)
                .role(UserRole.USER)
                .country(Country.BULGARIA)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // Wallet
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(50))
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // Loyalty
        Loyalty loyalty = Loyalty.builder()
                .id(UUID.randomUUID())
                .member(user)
                .type(LoyaltyType.DEFAULT)
                .gamesPurchased(0)
                .build();

        // Връзки
        user.setWallet(wallet);

        loyalty.setMember(user);

        user.setBoughtGames(new ArrayList<>());
        user.setCreatedGames(new ArrayList<>());

        return user;
    }



    public static Game aTestGame(User publisher) {
        return Game.builder()
                .id(UUID.randomUUID())
                .title("Test Game")
                .price(BigDecimal.valueOf(15))
                .publisher(publisher)
                .releaseDate(LocalDateTime.now().minusDays(1))
                .build();
    }



    public static User aRandomAdmin() {
        User admin = aRandomUser();
        admin.setRole(UserRole.ADMIN);
        admin.setUsername("adminUser");
        admin.setEmail("admin@example.com");
        return admin;
    }


    public static Game aRandomGame() {
        User publisher = aRandomAdmin(); // или aRandomUser() ако искаш неадмин
        return aTestGame(publisher);
    }


}