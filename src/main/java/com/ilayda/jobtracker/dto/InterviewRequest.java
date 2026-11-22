package com.ilayda.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record InterviewRequest(
        @NotBlank(message = "Interview stage cannot be empty") String stage,
        @NotNull(message = "Interview date is required") LocalDateTime scheduledAt,
        String feedback
) {}