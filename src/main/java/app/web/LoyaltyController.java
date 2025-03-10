package app.web;

import app.game.service.*;
import app.loyalty.model.*;
import app.loyalty.service.*;
import app.security.*;
import app.user.model.*;
import app.user.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.util.*;


@Controller
@RequestMapping("/users")
public class LoyaltyController {

    private final UserService userService;
    private final GameService gameService;
    private final LoyaltyService loyaltyService;


    @Autowired
    public LoyaltyController(UserService userService, GameService gameService, LoyaltyService loyaltyService) {
        this.userService = userService;
        this.gameService = gameService;
        this.loyaltyService = loyaltyService;
    }


    // GET Loyalty Page  -  преглед на статуса на лоялност
    @GetMapping("/{userId}/loyalty")
    public ModelAndView getLoyaltyPage(@PathVariable UUID userId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        // Защита:  можеш да виждаш само своята лоялност
        if (!authenticationMetadata.getUserId().equals(userId)) {
            return new ModelAndView("redirect:/home");
        }

        User user = userService.getById(authenticationMetadata.getUserId());
        Loyalty loyalty = loyaltyService.getLoyaltyByUserId(userId);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("loyalty");

        modelAndView.addObject("user", user);
        modelAndView.addObject("loyalty", loyalty);
        modelAndView.addObject("discount", loyaltyService.getDiscountPercentage(userId) * 100);   // Преобразуваме в %

        return modelAndView;
    }
}