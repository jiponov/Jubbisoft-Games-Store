package app.web.mapper;

import app.user.model.*;
import app.web.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class DtoMapperUserUTest {

    @Test
    void givenHappyPath_whenMappingUserToUserEditRequest() {

        // Given
        User user = User.builder()
                .firstName("Lubo")
                .lastName("Jiponov")
                .email("lubo@abv.bg")
                .profilePicture("www.image.com")
                .build();

        // When
        UserEditRequest resultDto = DtoMapperUser.mapUserToUserEditRequest(user);

        // Then

        assertEquals(user.getFirstName(), resultDto.getFirstName());
        assertEquals(user.getLastName(), resultDto.getLastName());
        assertEquals(user.getEmail(), resultDto.getEmail());
        assertEquals(user.getProfilePicture(), resultDto.getProfilePicture());
    }
}