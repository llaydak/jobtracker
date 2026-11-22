package com.ilayda.jobtracker.controller;


import com.ilayda.jobtracker.dto.InterviewRequest;
import com.ilayda.jobtracker.dto.InterviewResponse;
import com.ilayda.jobtracker.entity.Interview;
import com.ilayda.jobtracker.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/interviews")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping
    public InterviewResponse createInterview(@PathVariable Long applicationId, @Valid @RequestBody InterviewRequest request){
        return interviewService.addInterview(applicationId, request);
    }

    @GetMapping
    public List<InterviewResponse> getInterviews(@PathVariable Long applicationId){
        return interviewService.getInterviewsByApplication(applicationId);
    }
}
