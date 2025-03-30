package app;

import app.game.model.*;
import app.game.repository.GameRepository;
import app.game.service.GameService;
import app.loyalty.model.Loyalty;
import app.loyalty.repository.LoyaltyRepository;
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

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UserInsufficientFundsTransactionITest {

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LoyaltyRepository loyaltyRepository;

    @Autowired
    private UserRepository userRepository;

    private User buyerUser;
    private User creatorUser;
    private Game expensiveGame;


    @BeforeEach
    void setUp() {
        creatorUser = userService.register(new RegisterRequest("creator", "pass123", Country.BULGARIA));
        buyerUser = userService.register(new RegisterRequest("poor_user", "weakpass", Country.BULGARIA));

        // safety net: инициализиране на boughtGames
        if (buyerUser.getBoughtGames() == null) {
            buyerUser.setBoughtGames(new ArrayList<>());
        }

        Wallet buyerWallet = walletRepository.findByOwner(buyerUser).orElseThrow();
        buyerWallet.setBalance(BigDecimal.valueOf(5.00));    // недостатъчен баланс
        walletRepository.save(buyerWallet);

        expensiveGame = Game.builder()
                .title("Overpriced Game")
                .price(BigDecimal.valueOf(100.00))
                .publisher(creatorUser)
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .description("Too expensive!")
                .imageCoverUrl("none.jpg")
                .genre(Genre.RPG)
                .purchasedByUsers(new ArrayList<>())
                .build();

        gameRepository.save(expensiveGame);
    }


    // Създава потребител с 5.00 лв в портфейла. Опитва се да купи игра за 100.00 лв. Проверява, че транзакцията се проваля.
    // Не получава играта. Балансът остава същият.  loyalty точките не се пипат
    @Test
    void testPurchaseFailsWhenInsufficientFunds() {
        var transaction = gameService.purchaseGame(expensiveGame, buyerUser);

        // Проверка: статусът на транзакцията е FAILED
        assertThat(transaction.getStatus().name()).isEqualTo("FAILED");

        // Проверка: играта не е добавена към потребителя
        User updatedUser = userRepository.findById(buyerUser.getId()).orElseThrow();
        boolean ownsGame = updatedUser.getBoughtGames().stream()
                .anyMatch(g -> g.getId().equals(expensiveGame.getId()));
        assertThat(ownsGame).isFalse();

        // Проверка: балансът на портфейла не се е променил
        Wallet wallet = walletRepository.findByOwner(updatedUser).orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo("5.00");

        // Проверка: loyalty не е ъпдейтнат
        Loyalty loyalty = loyaltyRepository.findByMemberId(buyerUser.getId()).orElse(null);
        if (loyalty != null) {
            assertThat(loyalty.getGamesPurchased()).isEqualTo(0);
        }
    }
}