package app;

import app.game.model.Game;
import app.game.model.Genre;
import app.game.repository.GameRepository;
import app.game.service.GameService;
import app.user.model.Country;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateGameRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class GameCreateAndRetrieveITest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserService userService;

    @Test
    void findAllGames_shouldReturnTheCreatedOne() {
        // Given
        RegisterRequest userRequest = RegisterRequest.builder()
                .username("adminUser")
                .password("123123")
                .country(Country.BULGARIA)
                .build();

        User publisher = userService.register(userRequest);

        CreateGameRequest request = CreateGameRequest.builder()
                .title("Casual Racing")
                .description("Fast-paced arcade racing experience.")
                .price(new BigDecimal("2.49"))
                .genre(Genre.ADVENTURE)
                .imageCoverUrl("https://example.com/racing.jpg")
                .build();

        // When
        gameService.createNewGame(request, publisher);

        // Then
        Optional<Game> gameOpt = gameRepository.findByTitle("Casual Racing");
        assertTrue(gameOpt.isPresent(), "Expected game with title 'Casual Racing' to be present");
        assertThat(gameOpt.get().getTitle(), is("Casual Racing"));
    }

    @Test
    void createGame_shouldPersistCorrectly() {
        // Given
        User publisher = userService.register(RegisterRequest.builder()
                .username("creator123")
                .password("pass456")
                .country(Country.GERMANY)
                .build());

        CreateGameRequest request = CreateGameRequest.builder()
                .title("Mystic Lands")
                .description("Explore a vast and magical open world.")
                .price(new BigDecimal("14.99"))
                .genre(Genre.RPG)
                .imageCoverUrl("https://example.com/mystic.jpg")
                .build();

        // When
        gameService.createNewGame(request, publisher);

        // Then
        Optional<Game> gameOpt = gameRepository.findByTitle("Mystic Lands");
        assertTrue(gameOpt.isPresent());
        Game game = gameOpt.get();
        assertThat(game.getTitle(), is("Mystic Lands"));
        assertThat(game.getGenre(), is(Genre.RPG));
        assertThat(game.getPublisher().getUsername(), is("creator123"));

    }

}

/*

// @Test
    // void createGame_shouldPersistCorrectly() {
    //     // Given
    //     User publisher = userService.register(RegisterRequest.builder()
    //             .username("creator123")
    //             .password("pass456")
    //             .country(Country.GERMANY)
    //             .build());
    //
    //     CreateGameRequest request = CreateGameRequest.builder()
    //             .title("Mystic Lands")
    //             .description("Explore a vast and magical open world.")
    //             .price(new BigDecimal("14.99"))
    //             .genre(Genre.RPG)
    //             .imageCoverUrl("https://example.com/mystic.jpg")
    //             .build();
    //
    //     // When
    //     gameService.createNewGame(request, publisher);
    //
    //     // Then
    //     Optional<Game> gameOpt = gameRepository.findByTitle("Mystic Lands");
    //     assertTrue(gameOpt.isPresent());
    //     Game game = gameOpt.get();
    //     assertThat(game.getTitle(), is("Mystic Lands"));
    //     assertThat(game.getGenre(), is(Genre.RPG));
    //     assertThat(game.getPublisher().getUsername(), is("creator123"));
    //     assertThat(game.getIsAvailable(), is(false)); // по подразбиране
    // }

*/