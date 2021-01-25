package com.example.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lantian
 */
@SpringBootApplication
@EnableScheduling
public class WebsocketSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketSpringBootApplication.class, args);
    }

}
