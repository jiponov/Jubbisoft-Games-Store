package app.web.dto;

import app.game.model.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.*;
import java.time.*;


@Data
public class CreateGameRequest {

    @NotBlank(message = "Title cannot be empty.")
    @Size(min = 3, max = 30, message = "Title length must be between 3 and 30 characters!")
    private String title;

    @NotBlank(message = "Description cannot be empty.")
    @Size(min = 10, max = 1000, message = "Description length must be between 10 and 1000 characters!")
    private String description;

    private BigDecimal price;

    @NotNull(message = "You must select a genre!")
    private Genre genre;

    private String imageCoverUrl;
}