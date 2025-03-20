package app.notice.client;

import app.notice.client.dto.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@FeignClient(name = "notice-svc", url = "http://localhost:8081/api/v1/notices")
public interface NoticeClient {

    // ТЕСТОВ МЕТОД за Hello
    @GetMapping("/test")
    ResponseEntity<String> getHelloMessage(@RequestParam(name = "name") String name);


    @GetMapping("/download/{gameId}/{userId}")
    ResponseEntity<Resource> downloadNotice(@RequestParam(name = "gameId") UUID gameId, @RequestParam(name = "userId") UUID userId);

    @PostMapping
    ResponseEntity<Void> createNotice(@RequestBody NoticeRequest request);


}