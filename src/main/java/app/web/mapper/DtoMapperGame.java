package app.web.mapper;

import app.game.model.*;
import app.web.dto.*;
import lombok.experimental.*;


@UtilityClass
public class DtoMapperGame {

    public static GameEditRequest mapGameToGameEditRequest(Game game) {

        GameEditRequest gameEditRequest = GameEditRequest.builder()
                .title(game.getTitle())
                .description(game.getDescription())
                .price(game.getPrice())
                .genre(game.getGenre())
                .imageCoverUrl(game.getImageCoverUrl())
                .build();

        return gameEditRequest;
    }

}