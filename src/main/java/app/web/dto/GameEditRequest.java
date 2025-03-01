package app.web.dto;

import app.game.model.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.*;

import java.math.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GameEditRequest {

    @NotBlank(message = "Title cannot be empty.")
    @Size(min = 3, max = 45, message = "Title length must be between 3 and 45 characters!")
    private String title;

    @NotBlank(message = "Description cannot be empty.")
    @Size(min = 10, max = 1000, message = "Description length must be between 10 and 1000 characters!")
    private String description;

    @NotNull(message = "Price cannot be null.")
    @Positive(message = "Price must be a positive number.")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;


    @NotNull(message = "You must select a genre!")
    private Genre genre;

    @NotBlank(message = "Image URL cannot be empty.")
    @URL(message = "Requires correct web link format")
    private String imageCoverUrl;
}