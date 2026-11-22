package com.ilayda.jobtracker.dto;

import java.time.LocalDateTime;

public record InterviewResponse(
        Long id,
        String stage,
        LocalDateTime scheduledAt,
        String feedback,
        Long jobApplicationId
) {}