package app.scheduler;

import app.user.service.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import app.scheduler.UserInactivityScheduler;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserInactivitySchedulerUTest {

    @Mock
    private UserService userService;

    private static final int INACTIVITY_DAYS_LIMIT = 60;

    @InjectMocks
    private UserInactivityScheduler userInactivityScheduler;

    // Има неактивни → извиква deactivateUsers()
    @Test
    void givenInactiveUsersExist_whenDeactivateInactiveUsers_thenDeactivateCalled() {
        // Given
        List<User> inactiveUsers = List.of(
                User.builder().id(UUID.randomUUID()).isActive(true).build(),
                User.builder().id(UUID.randomUUID()).isActive(true).build()
        );

        when(userService.getInactiveUsersByLastActivity(INACTIVITY_DAYS_LIMIT)).thenReturn(inactiveUsers);

        // When
        userInactivityScheduler.deactivateInactiveUsers();

        // Then
        verify(userService).getInactiveUsersByLastActivity(INACTIVITY_DAYS_LIMIT);
        verify(userService).deactivateUsers(inactiveUsers);
    }

    // Няма неактивни → не се извиква нищо
    @Test
    void givenNoInactiveUsers_whenDeactivateInactiveUsers_thenNoDeactivation() {
        // Given
        when(userService.getInactiveUsersByLastActivity(INACTIVITY_DAYS_LIMIT)).thenReturn(Collections.emptyList());

        // When
        userInactivityScheduler.deactivateInactiveUsers();

        // Then
        verify(userService).getInactiveUsersByLastActivity(INACTIVITY_DAYS_LIMIT);
        verify(userService, never()).deactivateUsers(any());
    }


}