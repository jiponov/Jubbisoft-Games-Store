package app.web;

import app.game.model.*;
import app.security.*;
import app.user.model.*;
import app.user.service.*;
import app.web.dto.*;
import app.web.mapper.*;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.access.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.util.*;


@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    // /users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);
        modelAndView.addObject("user", user);

        return modelAndView;
    }


    // /users/{id}/profile
    @GetMapping("/{id}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID id) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapperUser.mapUserToUserEditRequest(user));

        return modelAndView;
    }


    // /users/{id}/profile
    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid UserEditRequest userEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(id);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile-menu");

            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);

            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);

        return new ModelAndView("redirect:/home");
    }


    // /users/{id}/status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public String switchUserStatus(@PathVariable UUID id) {

        userService.switchStatus(id);

        return "redirect:/users";
    }


    // /users/{id}/role
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public String switchUserRole(@PathVariable UUID id) {

        userService.switchRole(id);

        return "redirect:/users";
    }
}