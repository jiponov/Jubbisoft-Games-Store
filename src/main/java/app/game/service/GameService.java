package app.game.service;

import app.game.model.*;
import app.game.repository.*;
import app.loyalty.service.*;
import app.shared.exception.*;
import app.transaction.model.*;
import app.user.model.*;
import app.user.service.*;
import app.wallet.model.*;
import app.wallet.service.*;
import app.web.dto.*;
import jakarta.validation.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class GameService {

    private final GameRepository gameRepository;

    private final UserService userService;
    private final WalletService walletService;
    private final LoyaltyService loyaltyService;


    @Autowired
    public GameService(GameRepository gameRepository, UserService userService, WalletService walletService, LoyaltyService loyaltyService) {
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.walletService = walletService;
        this.loyaltyService = loyaltyService;
    }


    // CREATE
    public void createNewGame(CreateGameRequest createGameRequest, User user) {

        // дали администраторът реално съществува
        if (user == null) {
            throw new DomainException("Publisher cannot be null");
        }

        Optional<Game> optionalTitle = gameRepository.findByTitle(createGameRequest.getTitle());

        if (optionalTitle.isPresent()) {
            throw new DomainException("Title [%s] already exist.".formatted(createGameRequest.getTitle()));
        }

        if (createGameRequest.getPrice() == null || createGameRequest.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Price must be a positive value.");
        }

        Game game = Game.builder()
                .publisher(user)    // Администраторът се записва като publisher
                .title(createGameRequest.getTitle())
                .description(createGameRequest.getDescription())
                .price(createGameRequest.getPrice())
                .genre(createGameRequest.getGenre())
                .isAvailable(false)    // Играта е недостъпна по подразбиране
                .imageCoverUrl(createGameRequest.getImageCoverUrl())
                .releaseDate(LocalDateTime.now())    // Датата на пускане е днес
                .updatedOn(LocalDateTime.now())
                .build();

        gameRepository.save(game);
        log.info("Successfully create new game with Title: [%s].".formatted(game.getTitle()));
    }


    //@Transactional(readOnly = true)    // Гарантира, че винаги чете от базата
    // get ALL by PUBLISHERID
    public List<Game> getAllGamesByPublisherId(UUID publisherId) {
        return gameRepository.findAllByPublisherIdOrderByReleaseDateDesc(publisherId);
    }


    // get ALL GAMES
    public List<Game> getAllGames() {
        List<Game> allGames = gameRepository.findAll();

        return allGames;
    }


    // get ALL AVAILABLE GAMES  -  true
    public List<Game> getAllAvailableGames() {
        return gameRepository.findAllByIsAvailableTrueOrderByReleaseDateDesc();
    }


    // get ONE game
    public Game getGameById(UUID gameId) {
        return gameRepository
                .findById(gameId)
                .orElseThrow(() -> new DomainException("Game with id [%s] does not exist.".formatted(gameId)));
    }


    // delete ONE game
    public void deleteGameById(UUID gameId) {
        gameRepository.deleteById(gameId);
    }


    // change status .isAvailable()  ->   true/false
    public void toggleAvailability(UUID gameId) {

        Game game = getGameById(gameId);

        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found!");
        }

        if (game.isAvailable()) {
            game.setAvailable(false);
        } else {
            game.setAvailable(true);
        }

        gameRepository.save(game);

    }


    // Проверка за title и избягване на дублиране с DB
    // Премахване на излишни интервали към title
    // Сетване на новите стойности от gameEditRequest
    // Запазване в база данни.
    // edit на GAME
    public void editGameDetails(UUID gameId, GameEditRequest gameEditRequest) {

        Game game = getGameById(gameId);

        // 1. Премахване на празни интервали от заглавието и снимката
        // Trim inputs
        String title = gameEditRequest.getTitle();
        String imageCoverUrl = gameEditRequest.getImageCoverUrl() != null ? gameEditRequest.getImageCoverUrl().trim() : "";

        if (title != null) {
            title = title.trim();
            if (title.isEmpty()) {
                throw new DomainException("Title cannot be empty!");
            }
        }

        // 2. Проверка дали заглавието вече се използва от друга игра
        Optional<Game> existingGame = gameRepository.findByTitle(title);

        if (existingGame.isPresent() && !existingGame.get().getId().equals(game.getId())) {
            throw new DomainException("Title is already in use! Choose another title.");
        }


        // 3. Проверка и почистване на description
        String description = gameEditRequest.getDescription();
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("Description cannot be empty!");
        }

        // 4. Проверка за неподходяща цена (примерно 0 или отрицателна)
        if (gameEditRequest.getPrice() == null || gameEditRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Price must be greater than 0!");
        }

        // 5. Проверка дали genre е null
        if (gameEditRequest.getGenre() == null) {
            throw new DomainException("You must select a genre!");
        }

        // 6. Проверка дали URL е празен
        String imageUrl = gameEditRequest.getImageCoverUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new DomainException("Image URL cannot be empty!");
        }


        // вече тук ОБНОВЯВАМЕ:
        game.setTitle(title);
        game.setDescription(description);
        game.setPrice(gameEditRequest.getPrice());
        game.setGenre(gameEditRequest.getGenre());

        // Ако USER остави полето празно, запазваме съществуващото изображение или даваме дефолтнато
        if (imageCoverUrl.isEmpty()) {
            game.setImageCoverUrl(game.getImageCoverUrl() != null ? game.getImageCoverUrl()
                    : "https://eapi.pcloud.com/getpubthumb?code=XZCUYlZHul3dwxwhr88Ezb8Fcfow4PsV4Xk&size=800x800&format=png");
        } else {
            game.setImageCoverUrl(imageCoverUrl);
        }

        game.setUpdatedOn(LocalDateTime.now());

        // game.set
        gameRepository.save(game);
    }


    public boolean isTitleInUseByAnotherGame(UUID gameId, String title) {
        Optional<Game> existingGame = gameRepository.findByTitle(title);
        return existingGame.isPresent() && !existingGame.get().getId().equals(gameId);
    }


    // -----------------------------  BUY GAME  -----------------------------


    @Transactional
    public Transaction purchaseGame(Game game, User user) {

        // 🔴 Проверяваме дали текущият user е Publisher на играта
        if (game.getPublisher().getId().equals(user.getId())) {
            throw new DomainException("You cannot buy your own created game!");
        }


        // 🔴 Проверяваме дали USER вече притежава дадена GAME
        boolean alreadyOwned = user
                .getBoughtGames()
                .stream()
                .anyMatch(g -> g.getId().equals(game.getId()));

        if (alreadyOwned) {
            throw new DomainException("You already own this game!");
        }


        // 🏷️ Проверяваме дали потребителят има отстъпка и автоматично намаляваме цената, ако потребителят е PREMIUM
        double discount = loyaltyService.getDiscountPercentage(user.getId());
        BigDecimal gamePrice = game.getPrice();

        BigDecimal discountAmount = gamePrice.multiply(BigDecimal.valueOf(discount));

        BigDecimal finalGamePrice = gamePrice.subtract(discountAmount);


        // 💳 Опит за плащане чрез WalletService  ->  TRANSACTION generated за покупка на Game!
        String chargeDescription = "Purchase of game '%s'".formatted(game.getTitle());
        Wallet wallet = user.getWallet();
        // връща transaction метода charge от walletservice
        Transaction transactionChargeResult = walletService.charge(user, wallet.getId(), finalGamePrice, chargeDescription);

        if (transactionChargeResult.getStatus() == TransactionStatus.FAILED) {
            log.warn("Charge for this game [%s] failed for user with id [%s]".formatted(game.getTitle(), user.getId()));

            //throw new DomainException("Transaction failed: Not enough funds or wallet issue.");
            return transactionChargeResult;
        }


        // ✅ if success:
        // купихме вече играта и я добавяме към библиотеката LIST на USER-а  (bought games)  >>
        user.getBoughtGames().add(game);

        // Добавяме потребителя към списъка на купилите играта
        game.getPurchasedByUsers().add(user);


        // 🔄 Запазваме и двете страни в базата; Гарантираме, че релацията @ManyToMany се обновява и в двете посоки!  >>
        // 1. запазваме User-a в db
        userService.saveUser(user);

        // 2. запазваме Game-a в db
        gameRepository.save(game);


        // 🎖️ обновяване на Loyalty GAMES STATUS (up +1)  ->  след покупка на GAME
        loyaltyService.updateLoyaltyAfterPurchase(user);

        return transactionChargeResult;
    }

    public List<Game> getMyPurchasedGames(User user) {
        return gameRepository.findAllByPurchasedByUsersOrderByReleaseDateDesc(user);
    }


}