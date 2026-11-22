package com.ilayda.jobtracker.controller;

import com.ilayda.jobtracker.dto.JobApplicationRequest;
import com.ilayda.jobtracker.dto.JobApplicationResponse;
import com.ilayda.jobtracker.entity.ApplicationStatus;
import com.ilayda.jobtracker.entity.JobApplication;
import com.ilayda.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @PostMapping
    public JobApplicationResponse CreatedApplication(@Valid @RequestBody JobApplicationRequest request){
        return jobApplicationService.createApplication(request);
    }

    @GetMapping
    public List<JobApplicationResponse> showAllApplications(){
        return jobApplicationService.getAllApplications();
    }

    @PatchMapping("/{id}/status")
    public JobApplicationResponse updateApplicationStatus(@PathVariable Long id, @RequestParam ApplicationStatus status){
        return jobApplicationService.updateStatus(id, status);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable Long id){
        jobApplicationService.deleteApplication(id);
    }

}
