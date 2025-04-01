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
import app.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UserBuyGameFullFlowITest {

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

    private User creatorUser;
    private User buyerUser;
    private Game testGame;


    @BeforeEach
    void setUp() {
        // Създаване на publisher (creator)
        creatorUser = userService.register(new RegisterRequest("game_creator", "pass123", Country.BULGARIA));

        // Създаване на купувач
        buyerUser = userService.register(new RegisterRequest("test_buyer", "123123", Country.BULGARIA));

        // Fix: гарантираме, че boughtGames няма да е null
        if (buyerUser.getBoughtGames() == null) {
            buyerUser.setBoughtGames(new ArrayList<>());
        }

        Wallet buyerWallet = walletRepository.findByOwner(buyerUser).orElseThrow();
        buyerWallet.setBalance(BigDecimal.valueOf(200.00));
        walletRepository.save(buyerWallet);

        // Създаване на игра
        testGame = new Game();
        testGame.setTitle("Epic Adventure");
        testGame.setPrice(BigDecimal.valueOf(50.00));
        testGame.setReleaseDate(LocalDateTime.now());
        testGame.setUpdatedOn(LocalDateTime.now());
        testGame.setDescription("Lovely game that can break your heart");
        testGame.setPublisher(creatorUser);   //  различен от купувача
        testGame.setImageCoverUrl("www.imagecover.jpg");
        testGame.setGenre(Genre.ADVENTURE);
        gameRepository.save(testGame);
    }


    @Test
    void testUserCanBuyGameAndWalletAndLoyaltyAreUpdated() {
        // when
        gameService.purchaseGame(testGame, buyerUser);

        // then
        User updatedUser = userRepository.findById(buyerUser.getId()).orElseThrow();

        // Проверка дали играта е закупена
        boolean hasGame = updatedUser.getBoughtGames().stream()
                .anyMatch(game -> game.getId().equals(testGame.getId()));

        assertThat(hasGame).isTrue();

        // Проверка на wallet баланса
        Wallet wallet = walletRepository.findByOwner(updatedUser).orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo("150.00");

        // Проверка на loyalty точки
        Loyalty loyalty = loyaltyRepository.findByMemberId(updatedUser.getId()).orElseThrow();
        assertThat(loyalty.getGamesPurchased()).isGreaterThan(0);
    }


    @Test
    void testLoyaltyDiscountAppliedAfterTwoPurchases() {
        // даден потребител с баланс
        Wallet buyerWallet = walletRepository.findByOwner(buyerUser).orElseThrow();
        buyerWallet.setBalance(BigDecimal.valueOf(300.00));
        walletRepository.save(buyerWallet);

        // Създаваме 3 игри с инициализиран purchasedByUsers списък
        Game game1 = Game.builder()
                .title("Game 1")
                .price(BigDecimal.valueOf(50.00))
                .publisher(creatorUser)
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .description("Desc")
                .imageCoverUrl("cover.jpg")
                .genre(Genre.ADVENTURE)
                .purchasedByUsers(new ArrayList<>())
                .build();

        Game game2 = Game.builder()
                .title("Game 2")
                .price(BigDecimal.valueOf(70.00))
                .publisher(creatorUser)
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .description("Description")
                .imageCoverUrl("cover.jpg")
                .genre(Genre.RPG)
                .purchasedByUsers(new ArrayList<>())
                .build();

        Game game3 = Game.builder()
                .title("Game 3")
                .price(BigDecimal.valueOf(80.00))
                .publisher(creatorUser)
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .description("Desc-Desc")
                .imageCoverUrl("cover.jpg")
                .genre(Genre.SPORTS)
                .purchasedByUsers(new ArrayList<>())
                .build();

        gameRepository.saveAll(List.of(game1, game2, game3));

        // Плащане на 1-ва и 2-ра игра (пълна цена)
        gameService.purchaseGame(game1, buyerUser);
        gameService.purchaseGame(game2, buyerUser);

        // Плащане на 3-та игра (очакваме 30% отстъпка -> 80 * 0.7 = 56.00)
        gameService.purchaseGame(game3, buyerUser);

        // Проверка на баланса
        Wallet updatedWallet = walletRepository.findByOwner(buyerUser).orElseThrow();
        BigDecimal expectedRemaining = BigDecimal.valueOf(300.00)
                .subtract(game1.getPrice())
                .subtract(game2.getPrice())
                .subtract(game3.getPrice().multiply(BigDecimal.valueOf(0.7))); // 30% off

        assertThat(updatedWallet.getBalance()).isEqualByComparingTo(expectedRemaining);

        // Проверка на loyalty
        Loyalty loyalty = loyaltyRepository.findByMemberId(buyerUser.getId()).orElseThrow();
        assertThat(loyalty.getGamesPurchased()).isEqualTo(3);
    }

}