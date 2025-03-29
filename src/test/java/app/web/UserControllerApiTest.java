package app.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import app.*;
import app.loyalty.model.*;
import app.loyalty.service.*;
import app.security.*;
import app.user.model.*;
import app.user.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

import java.util.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.web.dto.UserEditRequest;


@WebMvcTest(UserController.class)
public class UserControllerApiTest {
    @MockitoBean
    private  UserService userService;
    @MockitoBean
    private  LoyaltyService loyaltyService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putUnauthorizedRequestToSwitchRole_shouldReturn404AndNotFoundView() throws Exception {

        // 1. Build Request
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
    }

    @Test
    void adminCanSwitchUserRole_shouldRedirectToUsersPage() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "adminUser", "adminPass", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = put("/users/{id}/role", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).switchRole(userId);
    }

    @Test
    void adminCanSwitchUserStatus_shouldRedirectToUsersPage() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "adminUser", "adminPass", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = put("/users/{id}/status", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).switchStatus(userId);
    }

    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {

        // 1. Build Request
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).switchRole(any());
    }

    // Admin достъп
    @Test
    void getRequestToUsersEndpointWithAdmin_shouldReturnUsersView() throws Exception {
        // Given
        User testUser = TestBuilder.aRandomUser();
        UUID adminId = testUser.getId();

        AuthenticationMetadata principal = new AuthenticationMetadata(
                adminId,
                testUser.getUsername(),
                testUser.getPassword(),
                UserRole.ADMIN,
                true
        );

        when(userService.getById(adminId)).thenReturn(testUser);
        when(userService.getAllUsers()).thenReturn(List.of(testUser));
        when(userService.countActiveUsers()).thenReturn(1L);
        when(userService.countInactiveUsers()).thenReturn(0L);

        // When
        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal));

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users", "user", "activeCount", "inactiveCount"));

        verify(userService).getById(adminId);
        verify(userService).getAllUsers();
    }

    // Обикновен потребител
    @Test
    void getRequestToUsersEndpointWithUser_shouldReturnForbidden() throws Exception {
        // Given
        AuthenticationMetadata principal = new AuthenticationMetadata(
                UUID.randomUUID(), "User123", "123123", UserRole.USER, true
        );

        // When
        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal));

        // Then
        mockMvc.perform(request)
                .andExpect(status().isNotFound());


        verify(userService, never()).getById(any());
    }

    @Test
    void getRequestToUsersEndpointWithoutLogin_shouldRedirectToLogin() throws Exception {
        // When
        MockHttpServletRequestBuilder request = get("/users");

        // Then
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void getRequestToProfilePage_shouldReturnProfileMenuView() throws Exception {
        // Given
        User user = TestBuilder.aRandomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                UUID.randomUUID(), "adminUser", "adminPass", UserRole.ADMIN, true
        );

        // When
        MockHttpServletRequestBuilder request = get("/users/{id}/profile", user.getId())
                .with(user(principal));

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile-menu"))
                .andExpect(model().attributeExists("user", "userEditRequest"));

        verify(userService, times(1)).getById(user.getId());
    }


    // Успешна редакция
    @Test
    void putValidProfileEditRequest_shouldRedirectToHome() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(
                UUID.randomUUID(), "testUser", "pass", UserRole.USER, true
        );

        MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
                .with(user(principal))
                .with(csrf())
                .param("email", "newemail@example.com")
                .param("phoneNumber", "0888123456")
                .param("firstName", "John")
                .param("lastName", "Doe");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).editUserDetails(eq(userId), any(UserEditRequest.class));
    }

    // Грешна форма
    @Test
    void putInvalidProfileEditRequest_shouldReturnToProfileMenu() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = TestBuilder.aRandomUser(); // или мокни userService.getById(userId)

        when(userService.getById(userId)).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                UUID.randomUUID(), "testUser", "pass", UserRole.USER, true
        );

        MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
                .with(user(principal))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("phoneNumber", "not-a-phone") // invalid phone
                .param("email", "invalid-email-format") // невалиден имейл
                .param("firstName", "a".repeat(100)) // над 30 символа
                .param("lastName", "a".repeat(100)); // също

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile-menu"))
                .andExpect(model().attributeExists("user", "userEditRequest"));

        verify(userService, never()).editUserDetails(any(), any());
    }


    @Test
    void getAllUsers_asAdmin_shouldReturnUsersView() throws Exception {
        UUID adminId = UUID.randomUUID();

        User admin = TestBuilder.aRandomAdmin();
        admin.setId(adminId);

        List<User> users = List.of(TestBuilder.aRandomUser(), TestBuilder.aRandomUser());

        when(userService.getById(adminId)).thenReturn(admin);
        when(userService.getAllUsers()).thenReturn(users);
        when(userService.countActiveUsers()).thenReturn(2L);
        when(userService.countInactiveUsers()).thenReturn(1L);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                adminId, "admin", "pass", UserRole.ADMIN, true
        );

        mockMvc.perform(get("/users").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users", "user", "activeCount", "inactiveCount"));
    }

    @Test
    void getProfileMenu_shouldReturnProfileMenuView() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = TestBuilder.aRandomUser();
        user.setId(userId);

        when(userService.getById(userId)).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                UUID.randomUUID(), "testUser", "pass", UserRole.USER, true
        );

        mockMvc.perform(get("/users/{id}/profile", userId).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-menu"))
                .andExpect(model().attributeExists("user", "userEditRequest"));
    }


    @Test
    void switchUserStatus_asAdmin_shouldRedirect() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(
                adminId, "adminUser", "pass", UserRole.ADMIN, true
        );

        mockMvc.perform(put("/users/{id}/status", userId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).switchStatus(userId);
    }


    @Test
    void switchUserStatus_asUser_shouldBeForbidden() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID userUserId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(
                userUserId, "normalUser", "pass", UserRole.USER, true
        );

        mockMvc.perform(put("/users/{id}/status", userId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound());


        verify(userService, never()).switchStatus(any());
    }

    @Test
    void switchUserRole_asAdmin_shouldRedirectToUsersPage() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        User admin = TestBuilder.aRandomAdmin();
        admin.setId(adminId);

        doNothing().when(userService).switchRole(targetUserId);


        AuthenticationMetadata principal = new AuthenticationMetadata(
                adminId, "admin", "pass", UserRole.ADMIN, true
        );

        mockMvc.perform(put("/users/{id}/role", targetUserId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService).switchRole(targetUserId);
    }


    @Test
    void viewUser_asAdmin_shouldReturnViewUserPage() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User admin = TestBuilder.aRandomAdmin();
        admin.setId(adminId);

        User targetUser = TestBuilder.aRandomUser();
        targetUser.setId(targetUserId);

        Loyalty loyalty = Loyalty.builder()
                .id(UUID.randomUUID())
                .member(targetUser)
                .type(LoyaltyType.DEFAULT)
                .gamesPurchased(5)
                .build();

        when(userService.getById(adminId)).thenReturn(admin);
        when(userService.getById(targetUserId)).thenReturn(targetUser);
        when(loyaltyService.getLoyaltyByUserId(targetUserId)).thenReturn(loyalty);
        when(loyaltyService.getDiscountPercentage(targetUserId)).thenReturn(0.10); // 10%

        AuthenticationMetadata principal = new AuthenticationMetadata(
                adminId, "admin", "pass", UserRole.ADMIN, true
        );

        mockMvc.perform(get("/users/{id}/view", targetUserId)
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("view-user"))
                .andExpect(model().attributeExists("user", "viewSomeUser", "loyalty", "loyaltyDiscount"));
    }

    @Test
    void viewUser_asUser_shouldBeForbidden() throws Exception {
        UUID normalUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        AuthenticationMetadata principal = new AuthenticationMetadata(
                normalUserId, "normalUser", "pass", UserRole.USER, true
        );

        mockMvc.perform(get("/users/{id}/view", targetUserId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound()); // 👈 вместо isForbidden()
    }



}