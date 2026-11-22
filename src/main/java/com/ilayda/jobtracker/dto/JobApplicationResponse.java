package com.ilayda.jobtracker.dto;

import com.ilayda.jobtracker.entity.ApplicationStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        String position,
        ApplicationStatus status,
        String notes,
        LocalDateTime appliedAt,
        Long companyId,
        String companyName
) implements Serializable {}