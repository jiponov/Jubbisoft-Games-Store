package app.notice.client.dto;

import lombok.*;

import java.util.*;


@Data
@Builder
public class NoticeRequest {


    private UUID userId;

    private UUID gameId;

    private String title;

    private String description;

    private String username;

    private String gameUrl;

    private String publisher;
}