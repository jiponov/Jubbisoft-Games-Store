package app.web;

import app.game.model.*;
import app.game.service.*;
import app.notice.service.*;
import app.security.*;
import app.transaction.model.*;
import app.user.model.*;
import app.user.service.*;
import app.web.dto.*;
import app.web.mapper.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.util.*;


@Controller
@RequestMapping("/games")
public class GameController {

    private final UserService userService;
    private final GameService gameService;
    private final NoticeService noticeService;


    @Autowired
    public GameController(UserService userService, GameService gameService, NoticeService noticeService) {
        this.userService = userService;
        this.gameService = gameService;
        this.noticeService = noticeService;
    }


    // PUBLIC GAMES  -  EXPLORE button
    // /games/explore
    @GetMapping("/explore")
    public ModelAndView getAllPublicGames(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("games-public");

        if (authenticationMetadata != null && authenticationMetadata.getUserId() != null) {
            User user = userService.getById(authenticationMetadata.getUserId());
            modelAndView.addObject("user", user); // Добавяме user в модела
        } else {
            modelAndView.addObject("user", null); // Гарантираме, че user винаги съществува в Thymeleaf
        }

        // List<Game> allSystemGames = gameService.getAllGames();
        List<Game> allAvailablePublicGames = gameService.getAllAvailableGames();

        modelAndView.addObject("allAvailablePublicGames", allAvailablePublicGames);

        return modelAndView;
    }


    // GET for CREATE GAME  ->  ADMIN ROLE
    // /games/new
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public ModelAndView getNewGamePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка дали потребителят е логнат
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-game");

        modelAndView.addObject("user", user);
        modelAndView.addObject("createGameRequest", new CreateGameRequest());  // показваме празно DTO FORM

        return modelAndView;
    }


    // POST for CREATE GAME  ->  ADMIN ROLE
    // /games
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ModelAndView createGame(@Valid CreateGameRequest createGameRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка дали потребителят е логнат
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        if (bindingResult.hasErrors()) {

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("add-game");

            modelAndView.addObject("user", user);
            modelAndView.addObject("createGameRequest", createGameRequest);

            return modelAndView;
        }

        gameService.createNewGame(createGameRequest, user);

        return new ModelAndView("redirect:/games/owned");
    }


    // DELETE for GAME  ->  ADMIN ROLE
    // /games/{gameId}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{gameId}")
    public String deleteGame(@PathVariable UUID gameId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка дали потребителят е логнат
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return "redirect:/login";
        }

        // 2. Извличане на игра.  Взимаме играта по ID (методът хвърля грешка, ако играта не съществува)
        Game game = gameService.getGameById(gameId);

        // 3. Взимаме текущия потребител
        User user = userService.getById(authenticationMetadata.getUserId());

        // 4. Проверка за собственост.  Дали текущият потребител е собственикът на играта
        if (!game.getPublisher().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this game.");
        }

        gameService.deleteGameById(gameId);

        return "redirect:/games/owned";
    }


    // PUBLIC GAMES  for  USERS  (not for all - NOT LOGGED-IN)
    // /games/{gameId}
    @GetMapping("/{gameId}/explore")
    public ModelAndView viewGame(@PathVariable UUID gameId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        Game game = gameService.getGameById(gameId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game");

        if (authenticationMetadata != null && authenticationMetadata.getUserId() != null) {
            User user = userService.getById(authenticationMetadata.getUserId());
            modelAndView.addObject("user", user); // Добавяме user в модела
        } else {
            modelAndView.addObject("user", null); // Гарантираме, че user винаги съществува в Thymeleaf
        }

        modelAndView.addObject("game", game);

        return modelAndView;
    }


    // PUT  -  Share GAME
    // /games/{gameId}/availability
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{gameId}/availability")
    public String changeGameAvailability(@PathVariable UUID gameId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка дали потребителят е логнат
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return "redirect:/login";
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        // 2. Извличане на игра.  Взимаме играта по ID (методът хвърля грешка, ако играта не съществува)
        Game game = gameService.getGameById(gameId);

        // 3. Проверка за собственост.  Дали текущият потребител е собственикът на играта
        if (!game.getPublisher().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to change status of this game.");
        }

        gameService.toggleAvailability(gameId);

        return "redirect:/games/owned";
    }


    // MY OWNED GAMES  -  OWNED button
    // /games/owned
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/owned")
    public ModelAndView getAllOwnedGames(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("owned-games");

        // Проверка дали потребителят е логнат
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        modelAndView.addObject("user", user); // Добавяме user в модела

        // Взимаме всички игри, които са публикувани от текущия потребител
        List<Game> ownedGames = gameService.getAllGamesByPublisherId(user.getId());

        // Добавяме игрите в модела
        modelAndView.addObject("ownedGames", ownedGames);

        return modelAndView;
    }


    // /games/{gameId}/owned
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{gameId}/owned")
    public ModelAndView viewOwnedGame(@PathVariable UUID gameId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // Проверка за логнат потребител
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        Game game = gameService.getGameById(gameId);

        // Проверка за собственост.  Дали текущият потребител е собственикът на играта
        if (!game.getPublisher().getId().equals(user.getId())) {
            return new ModelAndView("redirect:/games/owned");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game-owned");

        modelAndView.addObject("user", user); // Добавяме user в модела

        modelAndView.addObject("game", game);

        return modelAndView;
    }


    // EDIT GAME - GET
    // /games/{gameId}/profile
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{gameId}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID gameId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка за логнат потребител
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        // 2. Извличане на игра.  Взимаме играта по ID (методът хвърля грешка, ако играта не съществува)
        Game game = gameService.getGameById(gameId);

        // 3. Проверка за собственост.  Дали текущият потребител е собственикът на играта
        if (!game.getPublisher().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this game.");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game-profile");

        modelAndView.addObject("user", user);
        modelAndView.addObject("game", game);
        modelAndView.addObject("gameEditRequest", DtoMapperGame.mapGameToGameEditRequest(game));

        return modelAndView;
    }


    // EDIT GAME - PUT
    // /games/{gameId}/profile
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{gameId}/profile")
    public ModelAndView updateGameProfile(@PathVariable UUID gameId, @Valid GameEditRequest gameEditRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка за логнат потребител
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        // 2. Извличане на игра.  Взимаме играта по ID (методът хвърля грешка, ако играта не съществува)
        Game game = gameService.getGameById(gameId);

        // 3. Проверка за собственост.  Дали текущият потребител е собственикът на играта
        if (!game.getPublisher().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this game.");
        }

        // 4. Валидация на формата.  Проверка за грешки във FORM - GameEditRequest
        if (bindingResult.hasErrors()) {

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("game-profile");

            modelAndView.addObject("user", user);
            modelAndView.addObject("game", game);
            modelAndView.addObject("gameEditRequest", gameEditRequest);

            return modelAndView;
        }

        // 5. Допълнителна валидация
        if (gameService.isTitleInUseByAnotherGame(gameId, gameEditRequest.getTitle())) {
            bindingResult.rejectValue("title", "error.gameEditRequest", "Title is already in use! Choose another title.");

            return new ModelAndView("game-profile")
                    .addObject("user", user)
                    .addObject("game", game)
                    .addObject("gameEditRequest", gameEditRequest);
        }

        // 6. Извикваме editGameDetails в GameService.  Обновяване на данните
        gameService.editGameDetails(gameId, gameEditRequest);


        return new ModelAndView("redirect:/games/owned");
        // return new ModelAndView("redirect:/games/{gameId}/owned");
        // пример:  /games/550e8400-e29b-41d4-a716-446655440000/owned
    }


    // -----------------------------  BUY GAME  -----------------------------


    // POST - Buy Game
    // /games/{gameId}/buy
    @PostMapping("/{gameId}/buy")
    public ModelAndView buyGame(@PathVariable UUID gameId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        // 1. Проверка за логнат потребител
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());
        Game game = gameService.getGameById(gameId);

        // Опит за покупка (ако няма баланс, ще хвърли грешка)
        Transaction buyGameResult = gameService.purchaseGame(game, user);

        // Презареждане на страницата на играта с допълнителни атрибути
        // ModelAndView modelAndView = new ModelAndView();
        // modelAndView.setViewName("game");
        // modelAndView.addObject("game", game);
        // modelAndView.addObject("successMessage", "Game purchased successfully!");

        return new ModelAndView("redirect:/transactions/" + buyGameResult.getId());
    }


    // /games/purchased
    @GetMapping("/purchased")
    public ModelAndView getPurchasedGames(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("purchased-games");

        // Проверка дали потребителят е логнат
        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        modelAndView.addObject("user", user); // Добавяме user в модела

        // Взимаме всички игри, които са публикувани от текущия потребител
        List<Game> purchasedGames = gameService.getMyPurchasedGames(user);

        // Добавяме игрите в модела
        modelAndView.addObject("purchasedGames", purchasedGames);

        return modelAndView;
    }


    // /games/notice/download/{gameId}/{userId}
    @GetMapping("/notice/download/{gameId}/{userId}")
    public ResponseEntity<Resource> downloadGameNotice(@PathVariable UUID gameId, @PathVariable UUID userId) {
        Resource file = noticeService.downloadNotice(gameId, userId);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=game-purchase.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(file);
    }

}