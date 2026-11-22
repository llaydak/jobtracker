package com.ilayda.jobtracker.client;

import com.ilayda.jobtracker.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class HttpNotificationSender implements NotificationSender {

    private final RestClient restClient = RestClient.create();

    @Value("${notification.service.url:http://localhost:8081}")
    private String notificationServiceUrl;

    @Override
    public void sendStatusChanged(NotificationRequest request) {
        try {
            restClient.post()
                    .uri(notificationServiceUrl + "/api/notifications")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Notification service unreachable, status update preserved. Error: {}", ex.getMessage());
        }
    }
}