package com.ilayda.jobtracker.service;

import com.ilayda.jobtracker.client.NotificationSender;
import com.ilayda.jobtracker.dto.JobApplicationRequest;
import com.ilayda.jobtracker.dto.JobApplicationResponse;
import com.ilayda.jobtracker.dto.NotificationRequest;
import com.ilayda.jobtracker.entity.ApplicationStatus;
import com.ilayda.jobtracker.entity.Company;
import com.ilayda.jobtracker.entity.JobApplication;
import com.ilayda.jobtracker.repository.CompanyRepository;
import com.ilayda.jobtracker.repository.JobApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationService {

    private final RestClient restClient = RestClient.create();
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyRepository companyRepository;
    private final NotificationSender notificationSender;

    @CacheEvict(value = "applications", allEntries = true)
    public JobApplicationResponse createApplication(JobApplicationRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        JobApplication application = new JobApplication();
        application.setPosition(request.position());
        application.setNotes(request.notes());
        application.setCompany(company);

        if (request.status() != null) {
            application.setStatus(request.status());
        }

        JobApplication saved = jobApplicationRepository.save(application);
        return mapToResponse(saved);
    }

    @Cacheable(value = "applications", key = "'all'")
    public List<JobApplicationResponse> getAllApplications() {
        return jobApplicationRepository.findAllWithCompany()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private JobApplicationResponse mapToResponse(JobApplication app) {
        return new JobApplicationResponse(
                app.getId(),
                app.getPosition(),
                app.getStatus(),
                app.getNotes(),
                app.getAppliedAt(),
                app.getCompany().getId(),
                app.getCompany().getName()
        );
    }

    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public JobApplicationResponse updateStatus(Long id, ApplicationStatus newStatus) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        application.setStatus(newStatus);
        JobApplicationResponse response = mapToResponse(application);

        try {
            NotificationRequest payload = new NotificationRequest(
                    response.id(),
                    response.position(),
                    response.companyName(),
                    newStatus.name()
            );
            notificationSender.sendStatusChanged(payload);
        } catch (Exception ex) {
            log.warn("Notification service unreachable, but status update was saved. Error: {}", ex.getMessage());
        }

        return response;

    }

    @CacheEvict(value = "applications", allEntries = true)
    public void deleteApplication(Long id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }
        jobApplicationRepository.deleteById(id);
    }
}