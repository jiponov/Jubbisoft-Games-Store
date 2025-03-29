package app.web;

import app.*;
import app.game.model.*;
import app.game.service.*;
import app.notice.service.*;
import app.security.*;
import app.shared.exception.*;
import app.transaction.model.*;
import app.user.model.*;
import app.user.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


import static org.hamcrest.Matchers.nullValue;


@WebMvcTest(GameController.class)
public class GameControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private GameService gameService;
    @MockitoBean
    private NoticeService noticeService;

    @Autowired
    private MockMvc mockMvc;


    // getAllPublicGames
    @Test
    void getAllPublicGames_shouldReturnPublicGamesView() throws Exception {
        List<Game> games = List.of(
                TestBuilder.aRandomGame(),
                TestBuilder.aRandomGame()
        );

        when(gameService.getAllAvailableGames()).thenReturn(games);

        mockMvc.perform(get("/games/explore"))
                .andExpect(status().isOk())
                .andExpect(view().name("games-public"))
                .andExpect(model().attributeExists("allAvailablePublicGames"))
                .andExpect(model().attribute("user", nullValue()));

        verify(gameService).getAllAvailableGames();
    }


    // getAllPublicGames
    @Test
    void getAllPublicGames_withLoggedUser_shouldIncludeUserInModel() throws Exception {
        User user = TestBuilder.aRandomUser();
        UUID userId = user.getId();
        List<Game> games = List.of(TestBuilder.aRandomGame());

        when(userService.getById(userId)).thenReturn(user);
        when(gameService.getAllAvailableGames()).thenReturn(games);

        AuthenticationMetadata principal = new AuthenticationMetadata(userId, user.getUsername(), user.getPassword(), UserRole.USER, true);

        mockMvc.perform(get("/games/explore").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("games-public"))
                .andExpect(model().attributeExists("allAvailablePublicGames", "user"))
                .andExpect(model().attribute("user", user));

        verify(gameService).getAllAvailableGames();
        verify(userService).getById(userId);
    }


    // getNewGamePage
    @Test
    void getNewGamePage_withAdmin_shouldReturnAddGameView() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        when(userService.getById(admin.getId())).thenReturn(admin);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(get("/games/new").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("add-game"))
                .andExpect(model().attributeExists("user", "createGameRequest"));

        verify(userService).getById(admin.getId());
    }


    // getNewGamePage
    @Test
    void getNewGamePage_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/games/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // createGame
    @Test
    void createGame_withValidData_shouldRedirectToOwnedGames() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        when(userService.getById(admin.getId())).thenReturn(admin);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(post("/games")
                        .with(user(principal))
                        .with(csrf())
                        .param("title", "Cool Game")
                        .param("description", "An epic adventure")
                        .param("price", "29.99")
                        .param("genre", "ACTION")
                        .param("imageUrl", "http://image.com/game.jpg")
                        .param("releaseDate", "2025-03-30T12:00")) // ISO_LOCAL_DATE_TIME
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/owned"));

        verify(gameService).createNewGame(any(), eq(admin));
    }


    // createGame
    @Test
    void createGame_withInvalidForm_shouldReturnToAddGameView() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        when(userService.getById(admin.getId())).thenReturn(admin);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(post("/games")
                        .with(user(principal))
                        .with(csrf())
                        .param("title", "") // invalid - required
                        .param("description", "") // invalid
                        .param("price", "-1") // invalid - negative
                        .param("genre", "") // invalid
                        .param("imageUrl", "not-a-url") // invalid format
                        .param("releaseDate", "")) // missing
                .andExpect(status().isOk()) // NOT redirect
                .andExpect(view().name("add-game"))
                .andExpect(model().attributeExists("user", "createGameRequest"));

        verify(gameService, never()).createNewGame(any(), any());
    }


    // deleteGame
    // Успешно изтриване от админ (който е собственик)
    @Test
    void deleteGame_asOwnerAdmin_shouldRedirectToOwnedGames() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(delete("/games/{gameId}", game.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/owned"));

        verify(gameService).deleteGameById(game.getId());
    }


    // deleteGame
    // Изтриване от админ, който НЕ е собственик → очакваме AccessDeniedException → 404
    @Test
    void deleteGame_asNonOwnerAdmin_shouldReturnNotFound() throws Exception {
        User owner = TestBuilder.aRandomAdmin();
        owner.setId(UUID.randomUUID());

        User anotherAdmin = TestBuilder.aRandomAdmin();
        anotherAdmin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(owner);

        when(userService.getById(anotherAdmin.getId())).thenReturn(anotherAdmin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                anotherAdmin.getId(), anotherAdmin.getUsername(), anotherAdmin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(delete("/games/{gameId}", game.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound()); // or isForbidden() depending on config

        verify(gameService, never()).deleteGameById(any());
    }


    // deleteGame
    // Потребител без логин → redirect към login
    @Test
    void deleteGame_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(delete("/games/{gameId}", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // viewGame
    // Не-логнат потребител (показва игра без user данни)
    @Test
    void viewGame_asGuest_shouldReturnGameViewWithoutUser() throws Exception {
        Game game = TestBuilder.aTestGame(TestBuilder.aRandomAdmin());

        when(gameService.getGameById(game.getId())).thenReturn(game);

        mockMvc.perform(get("/games/{gameId}/explore", game.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("game"))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("user", nullValue())); // Проверяваме, че user е null за гости

        verify(gameService).getGameById(game.getId());
    }


    // viewGame
    // Логнат потребител (показва игра и включва потребителя в модела)
    @Test
    void viewGame_asLoggedUser_shouldReturnGameViewWithUser() throws Exception {
        User user = TestBuilder.aRandomUser();
        Game game = TestBuilder.aTestGame(user);

        when(gameService.getGameById(game.getId())).thenReturn(game);
        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                user.getId(), user.getUsername(), user.getPassword(), UserRole.USER, true
        );

        mockMvc.perform(get("/games/{gameId}/explore", game.getId()).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("game"))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("user", user)); // Проверяваме, че user е в модела

        verify(gameService).getGameById(game.getId());
        verify(userService).getById(user.getId());
    }


    // Игра не съществува (когато играта не бъде намерена, очакваме 404)
    @Test
    void viewGame_gameNotFound_shouldReturnInternalServerError() throws Exception {
        // Arrange
        UUID gameId = UUID.randomUUID();

        when(gameService.getGameById(gameId)).thenThrow(new DomainException("Game not found"));

        // Act & Assert
        mockMvc.perform(get("/games/{gameId}/explore", gameId))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("internal-server-error"))
                .andExpect(model().attribute("errorClass", "DomainException"))
                .andExpect(model().attribute("errorLocalizedMessage", "Game not found"));

        verify(gameService).getGameById(gameId);
    }


    // Успешна промяна на достъпност
    @Test
    void changeGameAvailability_asOwnerAdmin_shouldRedirectToOwnedGames() throws Exception {
        // Arrange
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin); // играта е негова

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        // Act & Assert
        mockMvc.perform(put("/games/{gameId}/availability", game.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/owned"));

        verify(gameService).toggleAvailability(game.getId());
    }


    // Друг админ (не-собственик) → 404
    @Test
    void changeGameAvailability_asNonOwnerAdmin_shouldReturnNotFound() throws Exception {
        // Arrange
        User owner = TestBuilder.aRandomAdmin();
        owner.setId(UUID.randomUUID());

        User anotherAdmin = TestBuilder.aRandomAdmin();
        anotherAdmin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(owner);

        when(userService.getById(anotherAdmin.getId())).thenReturn(anotherAdmin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                anotherAdmin.getId(), anotherAdmin.getUsername(), anotherAdmin.getPassword(), UserRole.ADMIN, true
        );

        // Act & Assert
        mockMvc.perform(put("/games/{gameId}/availability", game.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(gameService, never()).toggleAvailability(any());
    }


    // Няма логнат потребител → redirect към login
    @Test
    void changeGameAvailability_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(put("/games/{gameId}/availability", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // Логнат админ вижда собствените си игри
    @Test
    void getAllOwnedGames_asLoggedAdmin_shouldReturnOwnedGamesView() throws Exception {
        // Arrange
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        List<Game> ownedGames = List.of(TestBuilder.aTestGame(admin));

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getAllGamesByPublisherId(admin.getId())).thenReturn(ownedGames);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        // Act & Assert
        mockMvc.perform(get("/games/owned").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("owned-games"))
                .andExpect(model().attribute("user", admin))
                .andExpect(model().attribute("ownedGames", ownedGames));

        verify(userService).getById(admin.getId());
        verify(gameService).getAllGamesByPublisherId(admin.getId());
    }


    // Без логнат потребител → redirect
    @Test
    void getAllOwnedGames_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/games/owned"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // Собственик админ вижда профила на играта
    @Test
    void viewOwnedGame_asOwnerAdmin_shouldReturnGameOwnedView() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(get("/games/{gameId}/owned", game.getId()).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("game-owned"))
                .andExpect(model().attribute("user", admin))
                .andExpect(model().attribute("game", game));

        verify(userService).getById(admin.getId());
        verify(gameService).getGameById(game.getId());
    }


    //  Админ, който НЕ е собственик → redirect към /games/owned
    @Test
    void viewOwnedGame_asNonOwnerAdmin_shouldRedirectToOwnedGames() throws Exception {
        User owner = TestBuilder.aRandomAdmin();
        owner.setId(UUID.randomUUID());

        User anotherAdmin = TestBuilder.aRandomAdmin();
        anotherAdmin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(owner);

        when(userService.getById(anotherAdmin.getId())).thenReturn(anotherAdmin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                anotherAdmin.getId(), anotherAdmin.getUsername(), anotherAdmin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(get("/games/{gameId}/owned", game.getId()).with(user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/owned"));

        verify(userService).getById(anotherAdmin.getId());
        verify(gameService).getGameById(game.getId());
    }


    // Гост (без логин) → redirect към login
    @Test
    void viewOwnedGame_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/games/{gameId}/owned", UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // Собственик админ отваря форма за редакция
    @Test
    void getGameProfile_asOwnerAdmin_shouldReturnGameProfileView() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(get("/games/{gameId}/profile", game.getId()).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("game-profile"))
                .andExpect(model().attribute("user", admin))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attributeExists("gameEditRequest"));

        verify(userService).getById(admin.getId());
        verify(gameService).getGameById(game.getId());
    }

    // Не-собственик админ → AccessDeniedException → статус 404
    @Test
    void getGameProfile_asNonOwnerAdmin_shouldReturnNotFound() throws Exception {
        User owner = TestBuilder.aRandomAdmin();
        owner.setId(UUID.randomUUID());

        User anotherAdmin = TestBuilder.aRandomAdmin();
        anotherAdmin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(owner);

        when(userService.getById(anotherAdmin.getId())).thenReturn(anotherAdmin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                anotherAdmin.getId(), anotherAdmin.getUsername(), anotherAdmin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(get("/games/{gameId}/profile", game.getId()).with(user(principal)))
                .andExpect(status().isNotFound());

        verify(userService).getById(anotherAdmin.getId());
        verify(gameService).getGameById(game.getId());
    }

    // Без логин → redirect към login
    @Test
    void getGameProfile_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/games/{gameId}/profile", UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // PUT /games/{gameId}/profile
    // Успешна редакция от админ-собственик → redirect към /games/owned
    @Test
    void updateGameProfile_withValidData_shouldRedirectToOwnedGames() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);
        when(gameService.isTitleInUseByAnotherGame(eq(game.getId()), any())).thenReturn(false);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(put("/games/{gameId}/profile", game.getId())
                        .with(user(principal))
                        .with(csrf())
                        .param("title", "New Game Title")
                        .param("description", "Updated description")
                        .param("price", "19.99")
                        .param("genre", "RPG")
                        .param("imageCoverUrl", "http://example.com/image.png") // ✅ това е ключът!
                        .param("releaseDate", "2025-05-01T12:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games/owned"));

        verify(gameService).editGameDetails(eq(game.getId()), any());
    }


    // PUT /games/{gameId}/profile
    // Форма с грешки → остава на game-profile и показва грешки
    @Test
    void updateGameProfile_withInvalidData_shouldReturnToGameProfileView() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(put("/games/{gameId}/profile", game.getId())
                        .with(user(principal))
                        .with(csrf())
                        .param("title", "") // invalid
                        .param("description", "")
                        .param("price", "-5") // invalid
                        .param("genre", "")
                        .param("imageUrl", "not-a-url")
                        .param("releaseDate", "")) // missing
                .andExpect(status().isOk())
                .andExpect(view().name("game-profile"))
                .andExpect(model().attributeExists("user", "game", "gameEditRequest"));

        verify(gameService, never()).editGameDetails(any(), any());
    }

    // PUT /games/{gameId}/profile
    // Заглавие, което вече се ползва от друга игра → връща се към game-profile със съобщение
    @Test
    void updateGameProfile_withUsedTitle_shouldReturnToGameProfileWithError() throws Exception {
        User admin = TestBuilder.aRandomAdmin();
        admin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(admin);

        when(userService.getById(admin.getId())).thenReturn(admin);
        when(gameService.getGameById(game.getId())).thenReturn(game);
        when(gameService.isTitleInUseByAnotherGame(eq(game.getId()), eq("Taken Title"))).thenReturn(true);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                admin.getId(), admin.getUsername(), admin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(put("/games/{gameId}/profile", game.getId())
                        .with(user(principal))
                        .with(csrf())
                        .param("title", "Taken Title")
                        .param("description", "Some desc")
                        .param("price", "10")
                        .param("genre", "ACTION")
                        .param("imageUrl", "http://example.com/image.png")
                        .param("releaseDate", "2025-06-01T12:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("game-profile"))
                .andExpect(model().attributeExists("user", "game", "gameEditRequest"));

        verify(gameService, never()).editGameDetails(any(), any());
    }

    // PUT /games/{gameId}/profile
    // Опит от админ, който не е собственик → AccessDeniedException → 404
    @Test
    void updateGameProfile_asNonOwnerAdmin_shouldReturnNotFound() throws Exception {
        User owner = TestBuilder.aRandomAdmin();
        User anotherAdmin = TestBuilder.aRandomAdmin();
        owner.setId(UUID.randomUUID());
        anotherAdmin.setId(UUID.randomUUID());

        Game game = TestBuilder.aTestGame(owner);

        when(userService.getById(anotherAdmin.getId())).thenReturn(anotherAdmin);
        when(gameService.getGameById(game.getId())).thenReturn(game);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                anotherAdmin.getId(), anotherAdmin.getUsername(), anotherAdmin.getPassword(), UserRole.ADMIN, true
        );

        mockMvc.perform(put("/games/{gameId}/profile", game.getId())
                        .with(user(principal))
                        .with(csrf())
                        .param("title", "Whatever")
                        .param("description", "x")
                        .param("price", "1")
                        .param("genre", "ACTION")
                        .param("imageUrl", "http://image")
                        .param("releaseDate", "2025-05-01T12:00"))
                .andExpect(status().isNotFound());

        verify(gameService, never()).editGameDetails(any(), any());
    }

    // PUT /games/{gameId}/profile
    // Без логин → redirect към login
    @Test
    void updateGameProfile_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(put("/games/{gameId}/profile", UUID.randomUUID())
                        .with(csrf())
                        .param("title", "Test Title")
                        .param("description", "desc")
                        .param("price", "10")
                        .param("genre", "ACTION")
                        .param("imageUrl", "http://image.com")
                        .param("releaseDate", "2025-05-01T12:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // POST /games/{gameId}/buy
    // Покупка с логнат потребител и валидна игра – очакваме redirect към /transactions/{id}
    @Test
    void buyGame_withValidUser_shouldRedirectToTransaction() throws Exception {
        User user = TestBuilder.aRandomUser();
        Game game = TestBuilder.aTestGame(TestBuilder.aRandomAdmin());
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());

        when(userService.getById(user.getId())).thenReturn(user);
        when(gameService.getGameById(game.getId())).thenReturn(game);
        when(gameService.purchaseGame(game, user)).thenReturn(transaction);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                user.getId(), user.getUsername(), user.getPassword(), UserRole.USER, true
        );

        mockMvc.perform(post("/games/{gameId}/buy", game.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions/" + transaction.getId()));

        verify(gameService).purchaseGame(game, user);
    }

    // POST /games/{gameId}/buy
    // Покупка без логнат потребител – очакваме redirect към /login
    @Test
    void buyGame_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/games/{gameId}/buy", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // POST /games/{gameId}/buy
    // Покупка с недостатъчен баланс – очакваме грешка и internal-server-error view
    @Test
    void buyGame_insufficientFunds_shouldReturnInternalServerError() throws Exception {
        User user = TestBuilder.aRandomUser();
        Game game = TestBuilder.aTestGame(TestBuilder.aRandomAdmin());

        when(userService.getById(user.getId())).thenReturn(user);
        when(gameService.getGameById(game.getId())).thenReturn(game);
        when(gameService.purchaseGame(game, user))
                .thenThrow(new DomainException("Insufficient funds"));

        AuthenticationMetadata principal = new AuthenticationMetadata(
                user.getId(), user.getUsername(), user.getPassword(), UserRole.USER, true
        );

        mockMvc.perform(post("/games/{gameId}/buy", game.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("internal-server-error"))
                .andExpect(model().attribute("errorClass", "DomainException"))
                .andExpect(model().attribute("errorLocalizedMessage", "Insufficient funds"));

        verify(gameService).purchaseGame(game, user);
    }

    // GET /games/purchased
    // Логнат потребител с налични закупени игри – очакваме view purchased-games
    @Test
    void getPurchasedGames_withLoggedUser_shouldReturnPurchasedGamesView() throws Exception {
        User user = TestBuilder.aRandomUser();
        List<Game> purchasedGames = List.of(
                TestBuilder.aTestGame(user),
                TestBuilder.aTestGame(user)
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(gameService.getMyPurchasedGames(user)).thenReturn(purchasedGames);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                user.getId(), user.getUsername(), user.getPassword(), UserRole.USER, true
        );

        mockMvc.perform(get("/games/purchased").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("purchased-games"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("purchasedGames", purchasedGames));

        verify(userService).getById(user.getId());
        verify(gameService).getMyPurchasedGames(user);
    }

    // GET /games/purchased
    // Гост (не логнат) – очакваме redirect към login
    @Test
    void getPurchasedGames_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/games/purchased"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


    // GET /games/notice/download/{gameId}/{userId}
    // Успешно сваляне на файл (expect 200 OK + файл за download)
    @Test
    void downloadGameNotice_validRequest_shouldReturnNoticeFile() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String fileContent = "This is your purchase confirmation.";
        ByteArrayResource resource = new ByteArrayResource(fileContent.getBytes());

        when(noticeService.downloadNotice(gameId, userId)).thenReturn(resource);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId, "testuser", "pass", UserRole.USER, true
        );

        mockMvc.perform(get("/games/notice/download/{gameId}/{userId}", gameId, userId)
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=game-purchase.txt"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(fileContent));

        verify(noticeService).downloadNotice(gameId, userId);
    }


    // GET /games/notice/download/{gameId}/{userId}
    // Ако няма файл за този game/user → 404 Not Found
    @Test
    void downloadGameNotice_fileNotFound_shouldReturn404() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(noticeService.downloadNotice(gameId, userId)).thenReturn(null);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId, "user", "password", UserRole.USER, true
        );

        mockMvc.perform(get("/games/notice/download/{gameId}/{userId}", gameId, userId)
                        .with(user(principal)))
                .andExpect(status().isNotFound());

        verify(noticeService).downloadNotice(gameId, userId);
    }


}