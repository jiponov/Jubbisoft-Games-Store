package app.web.mapper;

import app.user.model.*;
import app.web.dto.*;
import lombok.experimental.*;


@UtilityClass
public class DtoMapperUser {
    public static UserEditRequest mapUserToUserEditRequest(User user) {

        return UserEditRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}