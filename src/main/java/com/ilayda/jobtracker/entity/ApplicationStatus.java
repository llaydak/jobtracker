package com.ilayda.jobtracker.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


public enum ApplicationStatus {
    APPLIED,
    INTERVIEW,
    OFFER,
    REJECTED
}
