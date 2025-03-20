package app.notice.client.dto;

import lombok.*;

import java.time.*;
import java.util.*;


@Data
@Builder
public class NoticeResponse {

    private UUID id;

    private String title;

    private String description;

    private LocalDateTime timestamp;

    private String username;

    private String gameUrl;

    private String publisher;
}