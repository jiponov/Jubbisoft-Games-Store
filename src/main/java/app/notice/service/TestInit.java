package app.notice.service;

import app.notice.client.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;


// ТЕСТОВ МЕТОД за Hello
@Component
public class TestInit implements ApplicationRunner {

    private final NoticeClient noticeClient;


    @Autowired
    public TestInit(NoticeClient noticeClient) {
        this.noticeClient = noticeClient;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ResponseEntity<String> response = noticeClient.getHelloMessage("Test-Test");

        System.out.println(response.getBody());
    }
}