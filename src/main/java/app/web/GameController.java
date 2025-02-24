package app.web;

import app.game.service.*;
import app.user.model.*;
import app.user.service.*;
import app.web.dto.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
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


}