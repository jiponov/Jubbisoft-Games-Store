package app.web;

import app.treasury.model.*;
import app.treasury.service.*;
import app.security.*;
import app.user.model.*;
import app.user.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.support.*;


@Controller
@RequestMapping("/treasury")
public class TreasuryController {

    private final TreasuryService treasuryService;
    private final UserService userService;


    @Autowired
    public TreasuryController(TreasuryService treasuryService, UserService userService) {
        this.treasuryService = treasuryService;
        this.userService = userService;
    }


    // POST
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/give-money")
    public ModelAndView giveMoney(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, RedirectAttributes redirectAttributes) {

        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        treasuryService.giveMoneyToUser(authenticationMetadata.getUserId());

        redirectAttributes.addFlashAttribute("successMessage", "100 EUR has been added to your wallet!");

        return new ModelAndView("redirect:/treasury");
    }


    // GET
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ModelAndView getTreasury(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());
        Treasury treasury = treasuryService.getByName("Treasury vault");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("treasury");

        modelAndView.addObject("user", user);
        modelAndView.addObject("treasury", treasury);


        return modelAndView;
    }

}






/*

--IllegalStateException се обработва автоматично от GlobalExceptionHandler.
--TreasuryController остава чист, без try-catch.
--Ако няма достатъчно средства, потребителят ще бъде пренасочен обратно към /treasury, като ще види съобщението за грешка.
Глобален Exception Handler (GlobalExceptionHandler)
Създаваме нов клас, който ще хваща грешките глобално в Spring и ще добавя errorMessage към RedirectAttributes. :



import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalStateException(IllegalStateException ex, RedirectAttributes redirectAttributes) {
    log.error("ERROR: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return new ModelAndView("redirect:/treasury");
    }
}






Ако не използваме try-catch в контролера, може да прехвърлим грешката нагоре към Spring и да използваме глобален Exception Handler, който ще обработва IllegalStateException и ще добавя съобщението към RedirectAttributes.

*/