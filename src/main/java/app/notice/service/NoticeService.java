package app.notice.service;

import app.notice.client.*;
import app.notice.client.dto.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.util.*;


@Slf4j
@Service
public class NoticeService {

    private final NoticeClient noticeClient;


    @Autowired
    public NoticeService(NoticeClient noticeClient) {
        this.noticeClient = noticeClient;
    }


    public void createNotice(UUID userId, UUID gameId, String title, String content, String username, String gameUrl, String publisher) {

        NoticeRequest request = NoticeRequest.builder()
                .userId(userId)
                .gameId(gameId)
                .title(title)
                .description(content)
                .username(username)
                .gameUrl(gameUrl)
                .publisher(publisher)
                .build();

        ResponseEntity<Void> httpResponse;

        try {
            httpResponse = noticeClient.createNotice(request);
            log.info("Successfully created notice for user with ID: {}", userId);

            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notice-svc failed] Can't create notice to user with id = [%s]".formatted(userId));
            }

        } catch (Exception e) {
            log.warn("Can't create notice to user with id = [%s] due to 500 Internal Server Error.".formatted(userId));
        }
    }


    public Resource downloadNotice(UUID gameId, UUID userId) {
        try {
            ResponseEntity<Resource> response = noticeClient.downloadNotice(gameId, userId);

            if (response.getBody() == null) {
                log.warn("No notice found for gameId: {} and userId: {}", gameId, userId);
                return null;
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to download notice for gameId: {} and userId: {}", gameId, userId, e);
            return null;
        }
    }

}


/*

public ResponseEntity<Resource> download(UUID noticeId) {
        ResponseEntity<Resource> resource = noticeClient.downloadNotice(noticeId);
        return resource;
    }

    */