package com.ilayda.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@SpringBootApplication
@RestController
@RequestMapping("/api/notifications")
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }

    public record NotificationRequest(
            Long applicationId,
            String position,
            String companyName,
            String newStatus
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String sendNotification(@RequestBody NotificationRequest request) {
        log.info("NOTIFICATION TRIGGERED -> Application #{} | Company: {} | Position: {} | New Status: {}",
                request.applicationId(),
                request.companyName(),
                request.position(),
                request.newStatus());
        return "Notification received";
    }
}