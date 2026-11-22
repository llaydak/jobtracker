package com.ilayda.jobtracker.client;

import com.ilayda.jobtracker.dto.NotificationRequest;

public interface NotificationSender {
    void sendStatusChanged(NotificationRequest request);
}