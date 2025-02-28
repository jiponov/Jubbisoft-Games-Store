package app.web.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.validator.constraints.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserEditRequest {

    @Size(max = 30, message = "First name can't have more than 30 symbols")
    private String firstName;

    @Size(max = 30, message = "Last name can't have more than 30 symbols")
    private String lastName;

    @Email(message = "Requires correct email format")
    @Size(max = 255)
    private String email;

    @URL(message = "Requires valid URL format")
    private String profilePicture;
}