package app.game.service;

import app.game.model.*;
import app.game.repository.*;
import app.shared.exception.*;
import app.user.model.*;
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


    public void createNewGame(User user, String title, String description, BigDecimal price, Genre genre, String imageCoverUrl) {

        Game game = Game.builder()
                .publisher(user)
                .title(title)
                .description(description)
                .price(price)
                .genre(genre)
                .isAvailable(true)
                .imageCoverUrl(imageCoverUrl)
                .releaseDate(LocalDate.now())
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