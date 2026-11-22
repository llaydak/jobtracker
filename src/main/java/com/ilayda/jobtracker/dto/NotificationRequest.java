package com.ilayda.jobtracker.dto;

public record NotificationRequest(
        Long applicationId,
        String position,
        String companyName,
        String newStatus
) {}