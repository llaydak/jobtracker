package com.ilayda.jobtracker.service;

import com.ilayda.jobtracker.dto.InterviewRequest;
import com.ilayda.jobtracker.dto.InterviewResponse;
import com.ilayda.jobtracker.entity.ApplicationStatus;
import com.ilayda.jobtracker.entity.Interview;
import com.ilayda.jobtracker.entity.JobApplication;
import com.ilayda.jobtracker.repository.InterviewRepository;
import com.ilayda.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public InterviewResponse addInterview(Long applicationId, InterviewRequest request) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        try {
            application.prepareForInterview();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        Interview interview = new Interview();
        interview.setStage(request.stage());
        interview.setScheduledAt(request.scheduledAt());
        interview.setFeedback(request.feedback());
        interview.setJobApplication(application);

        Interview saved = interviewRepository.save(interview);
        return mapToResponse(saved);
    }

    public List<InterviewResponse> getInterviewsByApplication(Long applicationId) {
        if (!jobApplicationRepository.existsById(applicationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }
        return interviewRepository.findByJobApplicationId(applicationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private InterviewResponse mapToResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getStage(),
                interview.getScheduledAt(),
                interview.getFeedback(),
                interview.getJobApplication().getId()
        );
    }
}