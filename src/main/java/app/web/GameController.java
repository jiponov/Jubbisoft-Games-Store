package app.web;

import app.game.model.*;
import app.game.service.*;
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


    // /games/new    -    GET for CREATE GAME
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


    // CREATE GAME   -   POST
    // /games
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


    // DELETE game   -   DELETE
    // /games/{gameId}
    @DeleteMapping("/{gameId}")
    public String deleteGame(@PathVariable UUID gameId, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            return "redirect:/login";
        }

        gameService.deleteGameById(gameId);

        return "redirect:/home";
    }


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


    // PUT  -  Share GAME
    // /games/{gameId}/availability
    @PutMapping("/{gameId}/availability")
    public String shareGame(@PathVariable UUID gameId, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");

        if (userId == null) {
            return "redirect:/login";
        }

        gameService.availabilityChangeTrue(gameId);

        return "redirect:/home";
    }
}