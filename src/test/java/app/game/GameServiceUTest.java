package app.game;

import app.game.model.*;
import app.game.repository.*;
import app.game.service.*;
import app.loyalty.service.*;
import app.notice.service.*;
import app.transaction.model.*;
import app.user.model.*;
import app.user.service.*;
import app.wallet.service.*;
import app.web.dto.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import app.loyalty.service.*;
import app.security.*;
import app.shared.exception.*;
import app.user.model.*;
import app.user.model.User;
import app.user.repository.*;
import app.user.service.*;
import app.wallet.model.*;
import app.wallet.service.*;
import app.web.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;

import java.math.*;
import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GameServiceUTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserService userService;
    @Mock
    private WalletService walletService;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private NoticeService noticeService;

    @InjectMocks
    private GameService gameService;


    // createNewGame()  -  GameService
    // Валиден вход.
    // Валиден request + user  ->  Game се създава и save-ва
    @Test
    void givenValidRequest_whenCreateNewGame_thenGameIsSaved() {
        // Given
        User admin = User.builder().id(UUID.randomUUID()).username("admin").build();

        CreateGameRequest request = CreateGameRequest.builder()
                .title("Super Game")
                .description("A fun game")
                .price(new BigDecimal("19.99"))
                .genre(Genre.ADVENTURE)
                .imageCoverUrl("http://example.com/image.png")
                .build();

        when(gameRepository.findByTitle("Super Game")).thenReturn(Optional.empty());

        // When
        gameService.createNewGame(request, admin);

        // Then
        verify(gameRepository, times(1)).save(argThat(game ->
                game.getTitle().equals("Super Game") &&
                        game.getPublisher().equals(admin) &&
                        game.getPrice().compareTo(new BigDecimal("19.99")) == 0 &&
                        !game.isAvailable()
        ));
    }


    // createNewGame()  -  GameService
    // Null потребител
    // Няма подаден user (null)	- Хвърля DomainException
    @Test
    void givenNullUser_whenCreateNewGame_thenThrowsException() {
        // Given
        CreateGameRequest request = CreateGameRequest.builder()
                .title("Super Game")
                .price(new BigDecimal("10.00"))
                .genre(Genre.ACTION)
                .build();

        // When & Then
        assertThrows(DomainException.class, () -> gameService.createNewGame(request, null));
        verify(gameRepository, never()).save(any());
    }


    // createNewGame()  -  GameService
    // Дублиращо заглавие
    // Заглавието вече съществува - Хвърля GameAlreadyExistException
    @Test
    void givenExistingTitle_whenCreateNewGame_thenThrowsException() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).build();

        CreateGameRequest request = CreateGameRequest.builder()
                .title("Existing Game")
                .price(new BigDecimal("9.99"))
                .genre(Genre.RPG)
                .build();

        when(gameRepository.findByTitle("Existing Game")).thenReturn(Optional.of(new Game()));

        // When & Then
        assertThrows(GameAlreadyExistException.class, () -> gameService.createNewGame(request, user));
        verify(gameRepository, never()).save(any());
    }


    // createNewGame()  -  GameService
    // Отрицателна/нулева цена
    // Цена е null или отрицателна - Хвърля DomainException
    @Test
    void givenInvalidPrice_whenCreateNewGame_thenThrowsException() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).build();

        CreateGameRequest request = CreateGameRequest.builder()
                .title("Bad Game")
                .price(new BigDecimal("-5.00"))
                .genre(Genre.SPORTS)
                .build();

        when(gameRepository.findByTitle("Bad Game")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> gameService.createNewGame(request, user));
        verify(gameRepository, never()).save(any());
    }


    // getAllGamesByPublisherId()  -  GameService
    // Съществуват игри за този publisher	->   Връща списък с тях
    @Test
    void givenGamesByPublisherId_whenGetAllGamesByPublisherId_thenReturnGameList() {
        // Given
        UUID publisherId = UUID.randomUUID();

        List<Game> mockGames = List.of(
                Game.builder().id(UUID.randomUUID()).title("Game 1").build(),
                Game.builder().id(UUID.randomUUID()).title("Game 2").build()
        );

        when(gameRepository.findAllByPublisherIdOrderByReleaseDateDesc(publisherId)).thenReturn(mockGames);

        // When
        List<Game> result = gameService.getAllGamesByPublisherId(publisherId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Game 1", result.get(0).getTitle());
        verify(gameRepository, times(1)).findAllByPublisherIdOrderByReleaseDateDesc(publisherId);
    }


    // getAllGamesByPublisherId()  -  GameService
    // Няма игри за този publisher	->  Връща празен списък
    // Repository се извиква с правилния publisherId	->  Проверка чрез verify(...)
    @Test
    void givenNoGamesByPublisherId_whenGetAllGamesByPublisherId_thenReturnEmptyList() {
        // Given
        UUID publisherId = UUID.randomUUID();
        when(gameRepository.findAllByPublisherIdOrderByReleaseDateDesc(publisherId)).thenReturn(Collections.emptyList());

        // When
        List<Game> result = gameService.getAllGamesByPublisherId(publisherId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameRepository, times(1)).findAllByPublisherIdOrderByReleaseDateDesc(publisherId);
    }


    // getAllGames()  -  GameService
    // Игри съществуват	Връща списък с тях
    @Test
    void givenGamesInDatabase_whenGetAllGames_thenReturnList() {
        // Given
        List<Game> mockGames = List.of(
                Game.builder().id(UUID.randomUUID()).title("Game A").build(),
                Game.builder().id(UUID.randomUUID()).title("Game B").build()
        );

        when(gameRepository.findAll()).thenReturn(mockGames);

        // When
        List<Game> result = gameService.getAllGames();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Game A", result.get(0).getTitle());
        verify(gameRepository, times(1)).findAll();
    }


    // getAllGames()  -  GameService
    // Няма игри	->    Връща празен списък
    // Repository метод се извиква правилно	verify(...) проверка
    @Test
    void givenNoGamesInDatabase_whenGetAllGames_thenReturnEmptyList() {
        // Given
        when(gameRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Game> result = gameService.getAllGames();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameRepository, times(1)).findAll();
    }


    // getAllAvailableGames()  -  GameService
    // Има налични игри → връщат се
    @Test
    void givenAvailableGamesInDatabase_whenGetAllAvailableGames_thenReturnList() {
        // Given
        List<Game> availableGames = List.of(
                Game.builder().id(UUID.randomUUID()).title("Available 1").isAvailable(true).build(),
                Game.builder().id(UUID.randomUUID()).title("Available 2").isAvailable(true).build()
        );

        when(gameRepository.findAllByIsAvailableTrueOrderByReleaseDateDesc()).thenReturn(availableGames);

        // When
        List<Game> result = gameService.getAllAvailableGames();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Game::isAvailable));
        verify(gameRepository, times(1)).findAllByIsAvailableTrueOrderByReleaseDateDesc();
    }


    // getAllAvailableGames()  -  GameService
    // Няма налични игри → празен списък
    // Repository метод се извиква
    @Test
    void givenNoAvailableGames_whenGetAllAvailableGames_thenReturnEmptyList() {
        // Given
        when(gameRepository.findAllByIsAvailableTrueOrderByReleaseDateDesc()).thenReturn(Collections.emptyList());

        // When
        List<Game> result = gameService.getAllAvailableGames();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameRepository, times(1)).findAllByIsAvailableTrueOrderByReleaseDateDesc();
    }


    // getGameById()  -  GameService
    //  Игра съществува	- Връща Game
    @Test
    void givenExistingGameId_whenGetGameById_thenReturnGame() {
        // Given
        UUID gameId = UUID.randomUUID();
        Game mockGame = Game.builder()
                .id(gameId)
                .title("Test Game")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));

        // When
        Game result = gameService.getGameById(gameId);

        // Then
        assertNotNull(result);
        assertEquals("Test Game", result.getTitle());
        assertEquals(gameId, result.getId());
        verify(gameRepository, times(1)).findById(gameId);
    }


    // getGameById()  -  GameService
    // Игра не съществува	-   Хвърля DomainException
    //  Repository метод се извиква	- Проверка чрез verify(...)
    @Test
    void givenNonExistingGameId_whenGetGameById_thenThrowsDomainException() {
        // Given
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // When & Then
        DomainException exception = assertThrows(DomainException.class, () -> gameService.getGameById(gameId));

        assertTrue(exception.getMessage().contains("Game with id"));
        verify(gameRepository, times(1)).findById(gameId);
    }


    // deleteGameById()  -  GameService
    // Валидно gameId	->   Извиква се deleteById() точно веднъж
    @Test
    void givenGameId_whenDeleteGameById_thenRepositoryDeleteIsCalled() {
        // Given
        UUID gameId = UUID.randomUUID();

        // When
        gameService.deleteGameById(gameId);

        // Then
        verify(gameRepository, times(1)).deleteById(gameId);
    }


    // toggleAvailability()  -  GameService
    // Игра е isAvailable = true	->    Става false, записва се
    @Test
    void givenAvailableGame_whenToggleAvailability_thenGameBecomesUnavailable() {
        // Given
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder()
                .id(gameId)
                .title("Test Game")
                .isAvailable(true)
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // When
        gameService.toggleAvailability(gameId);

        // Then
        assertFalse(game.isAvailable());
        verify(gameRepository).save(game);
    }


    // toggleAvailability()  -  GameService
    // Игра е isAvailable = false	->   Става true, записва се
    @Test
    void givenUnavailableGame_whenToggleAvailability_thenGameBecomesAvailable() {
        // Given
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder()
                .id(gameId)
                .title("Another Game")
                .isAvailable(false)
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // When
        gameService.toggleAvailability(gameId);

        // Then
        assertTrue(game.isAvailable());
        verify(gameRepository).save(game);
    }


    // editGameDetails()  -  GameService
    // HAPPY PATH:  Валиден GameEditRequest	 ->   Промените се записват
    @Test
    void givenValidEditRequest_whenEditGameDetails_thenGameIsUpdatedAndSaved() {
        // Given
        UUID gameId = UUID.randomUUID();
        Game existingGame = Game.builder()
                .id(gameId)
                .title("Old Title")
                .imageCoverUrl("old.png")
                .build();

        GameEditRequest request = GameEditRequest.builder()
                .title("  New Title  ") // will be trimmed
                .description("Updated description")
                .price(new BigDecimal("29.99"))
                .genre(Genre.ADVENTURE)
                .imageCoverUrl("  newImage.jpg  ")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(existingGame));
        when(gameRepository.findByTitle("New Title")).thenReturn(Optional.empty());

        // When
        gameService.editGameDetails(gameId, request);

        // Then
        assertEquals("New Title", existingGame.getTitle());
        assertEquals("Updated description", existingGame.getDescription());
        assertEquals(new BigDecimal("29.99"), existingGame.getPrice());
        assertEquals(Genre.ADVENTURE, existingGame.getGenre());
        assertEquals("newImage.jpg", existingGame.getImageCoverUrl());

        verify(gameRepository).save(existingGame);
    }


    // editGameDetails()  -  GameService
    // Празен title	Хвърля DomainException
    @Test
    void givenEmptyTitle_whenEditGameDetails_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).title("Old").build();

        GameEditRequest request = GameEditRequest.builder()
                .title("   ")  // trimmed -> empty
                .description("Desc")
                .price(BigDecimal.TEN)
                .genre(Genre.RPG)
                .imageCoverUrl("img.png")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        assertThrows(DomainException.class, () -> gameService.editGameDetails(gameId, request));
    }


    // editGameDetails()  -  GameService
    // Title вече зает от друга игра	->   Хвърля DomainException
    @Test
    void givenTakenTitleByAnotherGame_whenEditGameDetails_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        UUID otherGameId = UUID.randomUUID();

        Game currentGame = Game.builder().id(gameId).title("Old").build();
        Game otherGame = Game.builder().id(otherGameId).title("Taken").build();

        GameEditRequest request = GameEditRequest.builder()
                .title("Taken")
                .description("Desc")
                .price(BigDecimal.TEN)
                .genre(Genre.ACTION)
                .imageCoverUrl("img.jpg")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(currentGame));
        when(gameRepository.findByTitle("Taken")).thenReturn(Optional.of(otherGame));

        assertThrows(DomainException.class, () -> gameService.editGameDetails(gameId, request));
    }


    // editGameDetails()  -  GameService
    // Празно или null описание ->	Хвърля DomainException
    @Test
    void givenEmptyDescription_whenEditGameDetails_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).title("Title").build();

        GameEditRequest request = GameEditRequest.builder()
                .title("Valid")
                .description("  ") // invalid
                .price(BigDecimal.ONE)
                .genre(Genre.SPORTS)
                .imageCoverUrl("img.png")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.findByTitle("Valid")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> gameService.editGameDetails(gameId, request));
    }


    // editGameDetails()  -  GameService
    // Цена е null или ≤ 0	Хвърля DomainException
    @Test
    void givenInvalidPrice_whenEditGameDetails_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).title("Game").build();

        GameEditRequest request = GameEditRequest.builder()
                .title("New")
                .description("Valid desc")
                .price(new BigDecimal("-5.00"))
                .genre(Genre.ACTION)
                .imageCoverUrl("img.jpg")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.findByTitle("New")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> gameService.editGameDetails(gameId, request));
    }


    // editGameDetails()  -  GameService
    // genre == null	->   Хвърля DomainException
    @Test
    void givenNullGenre_whenEditGameDetails_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).title("Game").build();

        GameEditRequest request = GameEditRequest.builder()
                .title("New")
                .description("Valid desc")
                .price(BigDecimal.TEN)
                .genre(null) // invalid
                .imageCoverUrl("img.jpg")
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.findByTitle("New")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> gameService.editGameDetails(gameId, request));
    }


    // editGameDetails()  -  GameService
    // Празен или null image URL	->   Хвърля DomainException
    @Test
    void givenEmptyImageUrl_whenEditGameDetails_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).title("Game").build();

        GameEditRequest request = GameEditRequest.builder()
                .title("Valid")
                .description("Description")
                .price(BigDecimal.TEN)
                .genre(Genre.RPG)
                .imageCoverUrl("  ")  // trimmed → empty
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.findByTitle("Valid")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> gameService.editGameDetails(gameId, request));
    }


    // isTitleInUseByAnotherGame()  -  GameService
    // Има друга игра със същото title	=>  true
    @Test
    void givenDifferentGameWithSameTitle_whenCheck_thenReturnTrue() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        Game otherGame = Game.builder()
                .id(otherId)
                .title("Duplicate")
                .build();

        when(gameRepository.findByTitle("Duplicate")).thenReturn(Optional.of(otherGame));

        // When
        boolean result = gameService.isTitleInUseByAnotherGame(gameId, "Duplicate");

        // Then
        assertTrue(result);
    }


    // isTitleInUseByAnotherGame()  -  GameService
    // Title е на същата игра	=>  false
    @Test
    void givenSameGameWithSameTitle_whenCheck_thenReturnFalse() {
        UUID gameId = UUID.randomUUID();

        Game sameGame = Game.builder()
                .id(gameId)
                .title("UniqueTitle")
                .build();

        when(gameRepository.findByTitle("UniqueTitle")).thenReturn(Optional.of(sameGame));

        boolean result = gameService.isTitleInUseByAnotherGame(gameId, "UniqueTitle");

        assertFalse(result);
    }


    // isTitleInUseByAnotherGame()  -  GameService
    // Няма игра с такова title  =>  false
    @Test
    void givenNoGameWithTitle_whenCheck_thenReturnFalse() {
        when(gameRepository.findByTitle("FreeTitle")).thenReturn(Optional.empty());

        boolean result = gameService.isTitleInUseByAnotherGame(UUID.randomUUID(), "FreeTitle");

        assertFalse(result);
    }


    // purchaseGame()  -  GameService
    // User е publisher на играта ( Потребител се опитва да купи собствената си игра)	-> Хвърля DomainException
    @Test
    void givenUserIsPublisher_whenPurchaseGame_thenThrowsException() {
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Game game = Game.builder().id(UUID.randomUUID()).publisher(user).build();

        assertThrows(DomainException.class, () -> gameService.purchaseGame(game, user));
    }


    // purchaseGame()  -  GameService
    // User вече притежава играта - Хвърля DomainException
    @Test
    void givenUserAlreadyOwnsGame_whenPurchaseGame_thenThrowsException() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder().id(gameId).publisher(User.builder().id(UUID.randomUUID()).build()).build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .boughtGames(List.of(Game.builder().id(gameId).build()))
                .build();

        assertThrows(DomainException.class, () -> gameService.purchaseGame(game, user));
    }


    // purchaseGame()  -  GameService
    // WalletService връща FAILED	- Връща failed Transaction, нищо не се запазва
    @Test
    void givenTransactionFailed_whenPurchaseGame_thenReturnsFailedTransaction() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User publisher = User.builder().id(UUID.randomUUID()).username("publisher").build();
        Game game = Game.builder().id(gameId).publisher(publisher).price(new BigDecimal("20.00")).title("My Game").purchasedByUsers(new ArrayList<>()).build();
        Wallet wallet = Wallet.builder().id(UUID.randomUUID()).build();

        User user = User.builder()
                .id(userId)
                .username("buyer")
                .wallet(wallet)
                .boughtGames(new ArrayList<>())
                .build();

        Transaction failedTransaction = Transaction.builder()
                .status(TransactionStatus.FAILED)
                .build();

        when(loyaltyService.getDiscountPercentage(userId)).thenReturn(0.0);
        when(walletService.charge(eq(user), eq(wallet.getId()), any(), anyString())).thenReturn(failedTransaction);

        // When
        Transaction result = gameService.purchaseGame(game, user);

        // Then
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(userService, never()).saveUser(any());
        verify(gameRepository, never()).save(any());
        verify(noticeService, never()).createNotice(any(), any(), any(), any(), any(), any(), any());
    }


    // purchaseGame()  -  GameService
    // Успешна покупка (всичко валидно) - Добавя се в boughtGames, запис в DB, loyalty update, notice
    @Test
    void givenValidPurchase_whenPurchaseGame_thenSuccessFlowIsExecuted() {
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User publisher = User.builder().id(UUID.randomUUID()).username("admin").build();

        Game game = Game.builder()
                .id(gameId)
                .title("Epic Game")
                .price(new BigDecimal("50.00"))
                .publisher(publisher)
                .purchasedByUsers(new ArrayList<>())
                .build();

        Wallet wallet = Wallet.builder().id(UUID.randomUUID()).build();

        User user = User.builder()
                .id(userId)
                .username("player")
                .wallet(wallet)
                .boughtGames(new ArrayList<>())
                .build();

        Transaction approvedTransaction = Transaction.builder()
                .status(TransactionStatus.APPROVED)
                .build();

        when(loyaltyService.getDiscountPercentage(userId)).thenReturn(0.10); // 10%
        when(walletService.charge(eq(user), eq(wallet.getId()), any(), anyString())).thenReturn(approvedTransaction);

        // When
        Transaction result = gameService.purchaseGame(game, user);

        // Then
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertTrue(user.getBoughtGames().contains(game));
        assertTrue(game.getPurchasedByUsers().contains(user));

        verify(userService).saveUser(user);
        verify(gameRepository).save(game);
        verify(loyaltyService).updateLoyaltyAfterPurchase(user);
        verify(noticeService).createNotice(eq(userId), eq(gameId), any(), any(), eq("player"), any(), eq("admin"));
    }


    // getMyPurchasedGames()  -  GameService
    // Потребител има закупени игри	- Връща списък с тях
    // Има игри → връщат се
    @Test
    void givenUserWithPurchasedGames_whenGetMyPurchasedGames_thenReturnList() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).username("player1").build();

        List<Game> games = List.of(
                Game.builder().title("Game A").build(),
                Game.builder().title("Game B").build()
        );

        when(gameRepository.findAllByPurchasedByUsersOrderByReleaseDateDesc(user)).thenReturn(games);

        // When
        List<Game> result = gameService.getMyPurchasedGames(user);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Game A", result.get(0).getTitle());
        verify(gameRepository).findAllByPurchasedByUsersOrderByReleaseDateDesc(user);
    }

    // getMyPurchasedGames()  -  GameService
    // Потребител няма игри	- Връща празен списък
    // Няма игри → празен списък
    @Test
    void givenUserWithNoPurchasedGames_whenGetMyPurchasedGames_thenReturnEmptyList() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).username("newUser").build();

        when(gameRepository.findAllByPurchasedByUsersOrderByReleaseDateDesc(user)).thenReturn(Collections.emptyList());

        // When
        List<Game> result = gameService.getMyPurchasedGames(user);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameRepository).findAllByPurchasedByUsersOrderByReleaseDateDesc(user);
    }


    // saveGame()  -  GameService
    // Валидна игра → успешно записана и върната
    // gameRepository.save() се извиква точно веднъж с коректния обект
    @Test
    void givenValidGame_whenSaveGame_thenGameIsSavedAndReturned() {
        // Given
        Game gameToSave = Game.builder()
                .title("Test Game")
                .price(new BigDecimal("15.00"))
                .build();

        Game savedGame = Game.builder()
                .id(UUID.randomUUID())
                .title("Test Game")
                .price(new BigDecimal("15.00"))
                .build();

        when(gameRepository.save(gameToSave)).thenReturn(savedGame);

        // When
        Game result = gameService.saveGame(gameToSave);

        // Then
        assertNotNull(result);
        assertEquals("Test Game", result.getTitle());
        assertEquals(new BigDecimal("15.00"), result.getPrice());
        verify(gameRepository, times(1)).save(gameToSave);
    }


}