package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.*;
import org.springframework.cloud.openfeign.*;
import org.springframework.scheduling.annotation.*;


@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}