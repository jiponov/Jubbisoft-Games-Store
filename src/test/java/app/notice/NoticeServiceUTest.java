package app.notice;

import app.notice.client.*;
import app.notice.service.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import app.notice.client.NoticeClient;
import app.notice.client.dto.NoticeRequest;
import app.notice.service.NoticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;


@ExtendWith(MockitoExtension.class)
public class NoticeServiceUTest {

    @Mock
    private NoticeClient noticeClient;

    @InjectMocks
    private NoticeService noticeService;

    // createNotice  -  NoticeService
    // 2xx от noticeClient → всичко е наред
    @Test
    void givenValidInput_whenCreateNotice_thenClientIsCalledWithCorrectRequest() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        String title = "New Game Released!";
        String content = "Check out your new game.";
        String username = "lubo";
        String gameUrl = "http://localhost:8080/games";
        String publisher = "admin";

        ResponseEntity<Void> successResponse = new ResponseEntity<>(HttpStatus.OK);
        when(noticeClient.createNotice(any(NoticeRequest.class))).thenReturn(successResponse);

        // When
        noticeService.createNotice(userId, gameId, title, content, username, gameUrl, publisher);

        // Then
        ArgumentCaptor<NoticeRequest> captor = ArgumentCaptor.forClass(NoticeRequest.class);
        verify(noticeClient).createNotice(captor.capture());

        NoticeRequest sentRequest = captor.getValue();
        assertEquals(userId, sentRequest.getUserId());
        assertEquals(gameId, sentRequest.getGameId());
        assertEquals(title, sentRequest.getTitle());
        assertEquals(content, sentRequest.getDescription());
        assertEquals(username, sentRequest.getUsername());
        assertEquals(gameUrl, sentRequest.getGameUrl());
        assertEquals(publisher, sentRequest.getPublisher());
    }

    // createNotice  -  NoticeService
    // 4xx/5xx статус → логва грешка
    @Test
    void given4xxOr5xxResponse_whenCreateNotice_thenLogError() {
        // Given
        when(noticeClient.createNotice(any())).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        noticeService.createNotice(
                UUID.randomUUID(), UUID.randomUUID(),
                "title", "desc", "user", "url", "pub");

        // Then
        verify(noticeClient).createNotice(any());       // Still called, but response not 2xx
    }

    // createNotice  -  NoticeService
    // Изключение → log.warn, без грешка нагоре
    @Test
    void givenExceptionThrown_whenCreateNotice_thenHandledAndLogged() {
        // Given
        when(noticeClient.createNotice(any())).thenThrow(new RuntimeException("Downstream error"));

        // When
        noticeService.createNotice(
                UUID.randomUUID(), UUID.randomUUID(),
                "title", "desc", "user", "url", "pub");

        // Then
        verify(noticeClient).createNotice(any());
        // No exception thrown up — handled internally
    }


    // downloadNotice  -  NoticeService
    // Успешен отговор с Resource → връща се Resource
    @Test
    void givenValidRequest_whenDownloadNotice_thenReturnResource() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Resource expectedResource = new ByteArrayResource("PDF Content".getBytes());

        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(expectedResource, HttpStatus.OK);
        when(noticeClient.downloadNotice(gameId, userId)).thenReturn(responseEntity);

        // When
        Resource result = noticeService.downloadNotice(gameId, userId);

        // Then
        assertNotNull(result);
        assertEquals(expectedResource, result);
        verify(noticeClient).downloadNotice(gameId, userId);
    }


    // downloadNotice  -  NoticeService
    // Успешен отговор, но body == null → връща null
    @Test
    void givenNullBody_whenDownloadNotice_thenReturnNull() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ResponseEntity<Resource> response = new ResponseEntity<>(null, HttpStatus.OK);

        when(noticeClient.downloadNotice(gameId, userId)).thenReturn(response);

        // When
        Resource result = noticeService.downloadNotice(gameId, userId);

        // Then
        assertNull(result);
        verify(noticeClient).downloadNotice(gameId, userId);
    }


    // downloadNotice  -  NoticeService
    // Изключение → връща null, логва се
    @Test
    void givenExceptionThrown_whenDownloadNotice_thenReturnNull() {
        // Given
        UUID gameId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(noticeClient.downloadNotice(gameId, userId)).thenThrow(new RuntimeException("Service unavailable"));

        // When
        Resource result = noticeService.downloadNotice(gameId, userId);

        // Then
        assertNull(result);
        verify(noticeClient).downloadNotice(gameId, userId);
    }


}