package com.ilayda.jobtracker.dto;

import com.ilayda.jobtracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobApplicationRequest(
        @NotNull(message = "Company ID is required") Long companyId,
        @NotBlank(message = "Position name cannot be empty") String position,
        ApplicationStatus status,
        String notes
) {}