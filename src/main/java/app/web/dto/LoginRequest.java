package app.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
public class LoginRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 6, message = "Username must be at least 6 symbols")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;
}