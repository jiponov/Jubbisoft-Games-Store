package app.web.dto;

import app.user.model.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 6, message = "Username must be at least 6 symbols")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;

    @NotNull
    private Country country;
}