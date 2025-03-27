package app.web.mapper;

import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.game.model.Game;
import app.game.model.Genre;
import app.web.dto.GameEditRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DtoMapperGameUTest {

    @Test
    void givenGame_whenMapToGameEditRequest_thenCorrectlyMapped() {
        // Given
        Game game = Game.builder()
                .id(UUID.randomUUID())
                .title("Jubbisoft Adventures")
                .description("Explore the magical world of Jubbisoft.")
                .price(new BigDecimal("59.99"))
                .genre(Genre.ADVENTURE)
                .imageCoverUrl("https://image.url/cover.png")
                .releaseDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        // When
        GameEditRequest dto = DtoMapperGame.mapGameToGameEditRequest(game);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("Jubbisoft Adventures");
        assertThat(dto.getDescription()).isEqualTo("Explore the magical world of Jubbisoft.");
        assertThat(dto.getPrice()).isEqualByComparingTo("59.99");
        assertThat(dto.getGenre()).isEqualTo(Genre.ADVENTURE);
        assertThat(dto.getImageCoverUrl()).isEqualTo("https://image.url/cover.png");
    }
}