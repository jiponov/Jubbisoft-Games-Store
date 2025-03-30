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
public class UserCannotBuyOwnGameITest {

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

    private User creatorUser;
    private Game ownGame;


    @BeforeEach
    void setUp() {
        creatorUser = userService.register(new RegisterRequest("game_dev", "devpass", Country.BULGARIA));

        Wallet wallet = walletRepository.findByOwner(creatorUser).orElseThrow();
        wallet.setBalance(BigDecimal.valueOf(100.00));
        walletRepository.save(wallet);

        if (creatorUser.getBoughtGames() == null) {
            creatorUser.setBoughtGames(new ArrayList<>());
        }

        ownGame = Game.builder()
                .title("Dev's Own Game")
                .price(BigDecimal.valueOf(30.00))
                .publisher(creatorUser)
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .description("Cannot be bought by author")
                .imageCoverUrl("own.jpg")
                .genre(Genre.STRATEGY)
                .purchasedByUsers(new ArrayList<>())
                .build();

        gameRepository.save(ownGame);
    }

    // Потребител (publisher) създава игра. Има пари в портфейла. Опитва се да си я купи. Очаква се DomainException, че не може да купи собствена игра.
    @Test
    void testUserCannotPurchaseOwnGame() {
        assertThatThrownBy(() -> gameService.purchaseGame(ownGame, creatorUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot buy your own created game");
    }
}