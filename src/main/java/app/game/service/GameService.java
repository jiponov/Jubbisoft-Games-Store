package app.game.service;

import app.game.model.*;
import app.game.repository.*;
import app.shared.exception.*;
import app.user.model.*;
import app.user.service.*;
import app.wallet.model.*;
import app.wallet.service.*;
import app.web.dto.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
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


    @Autowired
    public GameService(GameRepository gameRepository, UserService userService, WalletService walletService) {
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.walletService = walletService;
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
                .releaseDate(LocalDate.now())    // Датата на пускане е днес
                .build();

        gameRepository.save(game);
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
        return gameRepository.findAllByIsAvailableTrue();
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


    // -----------------------------  BUY GAME  -----------------------------


    public void purchaseGame(UUID gameId, UUID userId) {

        User user = userService.getById(userId);
        Game game = getGameById(gameId);

        // Проверява дали потребителят вече притежава играта.
        if (game.getPurchasedByUsers().contains(user)) {
            throw new DomainException("You already own this game.");
        }

        Wallet wallet = user.getWallet();

        // Проверява дали има достатъчно баланс.
        if (wallet.getBalance().compareTo(game.getPrice()) < 0) {
            throw new DomainException("Insufficient funds to purchase this game.");
        }

        // Намаляваме баланса
        wallet.setBalance(wallet.getBalance().subtract(game.getPrice()));

        // Добавя играта към списъка на закупените.
        game.getPurchasedByUsers().add(user);

        // walletRepository.save(wallet);
        // gameRepository.save(game);
    }

}


/*  ALTERNATIVE

// change to false
public void availabilityChangeFalse(UUID gameId) {
    gameRepository
            .findById(gameId)
            .ifPresent(game -> {

                game.setAvailable(false);
                gameRepository.save(game);
            });
}
*/