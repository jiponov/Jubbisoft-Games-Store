package app.scheduler;

import app.user.model.*;
import app.user.service.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;


// SCHEDULER:  намира активни потребители, но невлизали от над 60 дни (user.getUpdatedOn()) в платформата Jubbisoft и:  user.setActive(false)
@Slf4j
@Component
public class UserInactivityScheduler {

    private final UserService userService;

    private static final int INACTIVITY_DAYS_LIMIT = 60;

    // за тестване:
    // private static final int INACTIVITY_DAYS_LIMIT = 0;


    @Autowired
    public UserInactivityScheduler(UserService userService) {
        this.userService = userService;
    }


    // за тестване на всеки 30 секунди:
    // @Scheduled(cron = "*/30 * * * * *")

    // всеки ден в 03:00
    @Scheduled(cron = "0 0 3 * * *")
    public void deactivateInactiveUsers() {
        List<User> inactiveUsers = userService.getInactiveUsersByLastActivity(INACTIVITY_DAYS_LIMIT);

        if (!inactiveUsers.isEmpty()) {
            userService.deactivateUsers(inactiveUsers);
            log.info("Deactivated {} inactive users.", inactiveUsers.size());
        } else {
            log.info("No inactive users to deactivate.");
        }
    }
}