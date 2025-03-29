package app.web;

import app.*;
import app.security.*;
import app.shared.exception.*;
import app.user.model.*;
import app.user.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

import java.util.*;

import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    // ВАЖНО: Когато тествам контролери трябва да МОКНА всички депендънсита на този контролер с анотация @MockitoBean!
    @MockitoBean
    private UserService userService;

    // Използвам MockMvc за да изпращам заявки
    @Autowired
    private MockMvc mockMvc;

    // Send GET /
    // Result - view name index
    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/");

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/register");

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // model().attributeExists("registerRequest") - проверява дали има конкретен NOT NULL атрибут
        // model().attribute("registerRequest", instanceOf(RegisterRequest.class))
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/login");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Lub123")
                .formField("password", "123123")
                .formField("country", "BULGARIA")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {

        // 1. Build Request
        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Lub123")
                .formField("password", "123123")
                .formField("country", "BULGARIA")
                .with(csrf());


        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .formField("country", "BULGARIA")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).register(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {

        // 1. Build Request
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView2() throws Exception {
        // Given
        User testUser = TestBuilder.aRandomUser();
        when(userService.getById(any())).thenReturn(testUser);

        UUID userId = testUser.getId();
        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId,
                testUser.getUsername(),
                testUser.getPassword(),
                testUser.getRole(),
                true
        );

        // When
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getById(userId);
    }




    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin() throws Exception {
        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        verify(userService, never()).getById(any());
    }

    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin2() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/home");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());
    }


    @Test
    void getRequestToAboutUs_shouldReturnAboutUsViewWithUserInModel() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = aRandomUser();
        when(userService.getById(userId)).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId, user.getUsername(), user.getPassword(), user.getRole(), true);

        MockHttpServletRequestBuilder request = get("/about-us").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("about-us"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getRequestToTerms_shouldReturnTermsViewWithUserInModel() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = aRandomUser();
        when(userService.getById(userId)).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId, user.getUsername(), user.getPassword(), user.getRole(), true);

        MockHttpServletRequestBuilder request = get("/terms").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("terms"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getRequestToContact_shouldReturnContactViewWithUserInModel() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = aRandomUser();
        when(userService.getById(userId)).thenReturn(user);

        AuthenticationMetadata principal = new AuthenticationMetadata(
                userId, user.getUsername(), user.getPassword(), user.getRole(), true);

        MockHttpServletRequestBuilder request = get("/contact").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeExists("user"));
    }


}