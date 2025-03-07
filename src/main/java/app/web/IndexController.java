package app.web;

import app.security.*;
import app.user.model.*;
import app.user.service.*;
import app.web.dto.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.util.*;


@Controller
public class IndexController {

    private final UserService userService;


    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public ModelAndView getIndexPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");

        if (authenticationMetadata != null && authenticationMetadata.getUserId() != null) {
            User user = userService.getById(authenticationMetadata.getUserId());
            modelAndView.addObject("user", user); // Добавяме user в модела
        } else {
            modelAndView.addObject("user", null); // Гарантираме, че user винаги съществува в Thymeleaf
        }

        return modelAndView;
    }


    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");

        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password!");
        }

        return modelAndView;
    }


    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");

        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }


    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }


    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");

        modelAndView.addObject("user", user);

        return modelAndView;
    }

}