package app.web;

import app.shared.exception.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.support.*;
import org.springframework.web.servlet.resource.*;


@ControllerAdvice
public class GlobalExceptionAdvice {

    // ВАЖНО: При редирект не връщаме @ResponseStatus(...)!


    // USERNAME
    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExist(RedirectAttributes redirectAttributes, UsernameAlreadyExistException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);

        return "redirect:/register";
    }


    // EMAIL
    @ExceptionHandler(EmailAlreadyExistException.class)
    public String handleEmailAlreadyExist(RedirectAttributes redirectAttributes, EmailAlreadyExistException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("emailAlreadyExistMessage", message);

        return "redirect:/users/{id}/profile";
    }


    // GAME
    @ExceptionHandler(GameAlreadyExistException.class)
    public String handleGameAlreadyExist(RedirectAttributes redirectAttributes, GameAlreadyExistException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("gameAlreadyExistMessage", message);

        return "redirect:/games/new";
    }


    // TREASURY
    @ExceptionHandler(TreasuryIsEmptyException.class)
    public String handleTreasuryIsEmpty(RedirectAttributes redirectAttributes, TreasuryIsEmptyException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("treasuryIsEmptyMessage", message);

        return "redirect:/treasury";
    }


    // чрез  (Exception exception)  можем да видим КОЙ тип ex. се е хвърлил например
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,                    // непозволен достъп
            NoResourceFoundException.class,                 // невалиден endpoint
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("not-found");

        modelAndView.addObject("errorClass", exception.getClass().getSimpleName());
        modelAndView.addObject("errorLocalizedMessage", exception.getLocalizedMessage());

        return modelAndView;
    }


    // the father of ALL (Exception.class):  ANY other exceptions, which are NOT these from ABOVE code
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("internal-server-error");

        modelAndView.addObject("errorClass", exception.getClass().getSimpleName());
        modelAndView.addObject("errorLocalizedMessage", exception.getLocalizedMessage());

        return modelAndView;
    }
}