package app;

import app.game.model.*;
import app.game.repository.GameRepository;
import app.game.service.GameService;
import app.user.model.*;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UserCannotBuySameGameTwiceITest {

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User creator;
    private User buyer;
    private Game game;


    @BeforeEach
    void setUp() {
        creator = userService.register(new RegisterRequest("creator", "pass", Country.BULGARIA));
        buyer = userService.register(new RegisterRequest("buyer", "pass", Country.BULGARIA));

        Wallet wallet = walletRepository.findByOwner(buyer).orElseThrow();
        wallet.setBalance(BigDecimal.valueOf(200.00));
        walletRepository.save(wallet);

        if (buyer.getBoughtGames() == null) {
            buyer.setBoughtGames(new ArrayList<>());
        }

        game = Game.builder()
                .title("Only Once Game")
                .price(BigDecimal.valueOf(60.00))
                .publisher(creator)
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .description("You can only buy this once")
                .imageCoverUrl("cover.png")
                .genre(Genre.RPG)
                .purchasedByUsers(new ArrayList<>())
                .build();

        gameRepository.save(game);
    }


    // Купувач с валиден портфейл. Купува една и съща игра 2 пъти. Очакваме втората покупка да хвърли изключение с подходящо съобщение
    @Test
    void testUserCannotBuySameGameTwice() {
        // First purchase should succeed
        gameService.purchaseGame(game, buyer);

        // Second purchase should throw DomainException
        assertThatThrownBy(() -> gameService.purchaseGame(game, buyer))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already own this game");
    }
}