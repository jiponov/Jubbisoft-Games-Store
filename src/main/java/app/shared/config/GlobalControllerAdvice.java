package app.shared.config;

import app.security.*;
import app.user.model.*;
import app.user.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;


@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;


    @Autowired
    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }


    @ModelAttribute("user")
    public User addUserToModel(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (authenticationMetadata != null && authenticationMetadata.getUserId() != null) {

            User user = userService.getById(authenticationMetadata.getUserId());

            return user;
        }

        return null;
    }
}



/*


ВЗИМАМЕ логнатия потребител чрез @AuthenticationPrincipal.
Автоматично добавяме USER към всички Thymeleaf шаблони, без да се налага да го ДОБАВЯМЕ във всеки Controller !

// user вече е в Thymeleaf html шаблоните БЕЗ да го добавям ръчно
<p th:text="${user.username}"></p>


Специални случаи, в които трябва да ПРОВЕРЯ кода:
=>>>  АПИ контролери (REST API) – НЕ използват ModelAndView
Ако ИМАМ @RestController, Thymeleaf няма да се използва, затова @ControllerAdvice няма да влияе на тези класове.

Този API метод работи както преди, защото не използва ModelAndView и @ControllerAdvice не влияе на него:
Проверка:

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));                   //  API не използва Thymeleaf
    }
}


*/