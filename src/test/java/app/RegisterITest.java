package app;

import app.user.model.*;
import app.user.repository.*;
import app.user.service.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;
import app.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
// Integration Test (Load the complete Spring Application Context - all beans)
public class RegisterITest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser_shouldPersistUserInDB() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("john_test")
                .password("123456")
                .country(Country.BULGARIA)
                .build();

        // Act
        User registeredUser = userService.register(request);

        // Assert
        Optional<User> userFromDb = userRepository.findById(registeredUser.getId());
        assertTrue(userFromDb.isPresent());
        assertEquals("john_test", userFromDb.get().getUsername());
        assertEquals(Country.BULGARIA, userFromDb.get().getCountry());
    }
}