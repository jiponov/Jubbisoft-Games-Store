package app.web;

import app.game.model.*;
import app.game.service.*;
import app.security.*;
import app.user.model.*;
import app.user.service.*;
import app.web.dto.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.*;
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


    @Autowired
    public GameController(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }


    // PUBLIC GAMES  -  EXPLORE button
    // /games/explore
    @GetMapping("/explore")
    public ModelAndView getAllPublicGames(HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("games-public");

        // Взимаме user_id от сесията
        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId != null) {
            User user = userService.getById(userId);
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
    @RequireAdminRole
    @GetMapping("/new")
    public ModelAndView getNewGamePage(HttpSession session) {
        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-game");

        modelAndView.addObject("user", user);
        modelAndView.addObject("createGameRequest", new CreateGameRequest());  // показваме празно DTO FORM

        return modelAndView;
    }


    // POST for CREATE GAME  ->  ADMIN ROLE
    // /games
    @RequireAdminRole
    @PostMapping
    public ModelAndView createGame(@Valid CreateGameRequest createGameRequest, BindingResult bindingResult, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        if (bindingResult.hasErrors()) {

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("add-game");

            modelAndView.addObject("user", user);
            modelAndView.addObject("createGameRequest", createGameRequest);

            return modelAndView;
        }

        gameService.createNewGame(createGameRequest, user);

        return new ModelAndView("redirect:/home");
    }


    // DELETE for GAME  ->  ADMIN ROLE
    // /games/{gameId}
    @RequireAdminRole
    @DeleteMapping("/{gameId}")
    public String deleteGame(@PathVariable UUID gameId, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            return "redirect:/login";
        }

        gameService.deleteGameById(gameId);

        return "redirect:/games/owned";
    }


    // PUBLIC GAMES  for  USERS  (not for all - NOT LOGGED-IN)
    // /games/{gameId}
    @GetMapping("/{gameId}/explore")
    public ModelAndView viewGame(@PathVariable UUID gameId, HttpSession session) {

        Game game = gameService.getGameById(gameId);

        if (game == null) {
            // return new ModelAndView("redirect:/login");
            // Или да върне 404:
            // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found");
            return new ModelAndView("redirect:/");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game");

        // Взимаме user_id от сесията
        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId != null) {
            User user = userService.getById(userId);
            modelAndView.addObject("user", user); // Добавяме user в модела
        } else {
            modelAndView.addObject("user", null); // Гарантираме, че user винаги съществува в Thymeleaf
        }

        modelAndView.addObject("game", game);

        return modelAndView;
    }


    // PUT  -  Share GAME
    // /games/{gameId}/availability
    @RequireAdminRole
    @PutMapping("/{gameId}/availability")
    public String changeGameAvailability(@PathVariable UUID gameId, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            return "redirect:/login";
        }

        gameService.toggleAvailability(gameId);

        return "redirect:/games/{gameId}/owned";
    }


    // MY OWNED GAMES  -  OWNED button
    // /games/owned
    @RequireAdminRole
    @GetMapping("/owned")
    public ModelAndView getAllOwnedGames(HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("owned-games");

        // Взимаме user_id от сесията
        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            // Ако няма логнат потребител, редиректваме към Login
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(userId);
        modelAndView.addObject("user", user); // Добавяме user в модела

        // Взимаме всички игри, които са публикувани от текущия потребител
        List<Game> ownedGames = gameService.getAllGamesByPublisherId(userId);

        // Добавяме игрите в модела
        modelAndView.addObject("ownedGames", ownedGames);

        return modelAndView;
    }


    // PUBLIC GAMES  for  USERS  (not for all - NOT LOGGED-IN)
    // /games/{gameId}
    @RequireAdminRole
    @GetMapping("/{gameId}/owned")
    public ModelAndView viewOwnedGame(@PathVariable UUID gameId, HttpSession session) {

        Game game = gameService.getGameById(gameId);

        if (game == null) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game-owned");

        // Взимаме user_id от сесията
        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            // Ако няма логнат потребител, редиректваме към Login
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(userId);
        modelAndView.addObject("user", user); // Добавяме user в модела

        modelAndView.addObject("game", game);

        return modelAndView;
    }
}



/*this WORKS  >>

// PUBLIC GAMES  -  EXPLORE button
    // /games
    @GetMapping("/explore")
    public ModelAndView getAllPublicGames(HttpSession session) {

        // List<Game> allSystemGames = gameService.getAllGames();
        List<Game> allAvailablePublicGames = gameService.getAllAvailableGames();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("games-public");

        modelAndView.addObject("allAvailablePublicGames", allAvailablePublicGames);

        return modelAndView;
    }

    */

/* this WORKS  >>

      // PUBLIC GAMES  for  USERS  (not for all - NOT LOGGED-IN)
    // /games/{gameId}
    @GetMapping("/{gameId}")
    public ModelAndView viewGame(@PathVariable UUID gameId, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            return new ModelAndView("redirect:/login");
        }

        Game game = gameService.getGameById(gameId);

        if (game == null) {
            // return new ModelAndView("redirect:/login");
            // Или да върне 404:
            // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found");
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game");

        modelAndView.addObject("game", game);

        return modelAndView;
    }

    */