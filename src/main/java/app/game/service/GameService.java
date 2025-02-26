package app.game.service;

import app.game.model.*;
import app.game.repository.*;
import app.shared.exception.*;
import app.user.model.*;
import app.web.dto.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.math.*;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class GameService {

    private final GameRepository gameRepository;


    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    // CREATE
    public void createNewGame(CreateGameRequest createGameRequest, User user) {

        // дали администраторът реално съществува
        if (user == null) {
            throw new IllegalArgumentException("Publisher cannot be null");
        }

        if (createGameRequest.getPrice() == null || createGameRequest.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be a positive value.");
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

    // change to true
    public void availabilityChangeTrue(UUID gameId) {
        gameRepository
                .findById(gameId)
                .ifPresent(game -> {

                    game.setAvailable(true);
                    gameRepository.save(game);
                });
    }

    // change to false
    public void availabilityChangeFalse(UUID gameId) {
        gameRepository
                .findById(gameId)
                .ifPresent(game -> {

                    game.setAvailable(false);
                    gameRepository.save(game);
                });
    }
}