package app.web;

import app.security.*;
import app.transaction.model.*;
import app.transaction.service.*;
import app.user.model.*;
import app.user.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.util.*;


@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;


    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }


    @GetMapping
    public ModelAndView showAllTransactions(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        if (authenticationMetadata == null || authenticationMetadata.getUserId() == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = userService.getById(authenticationMetadata.getUserId());

        List<Transaction> transactions = transactionService.getAllByOwnerId(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transactions");

        modelAndView.addObject("user", user);
        modelAndView.addObject("transactions", transactions);

        return modelAndView;
    }

}