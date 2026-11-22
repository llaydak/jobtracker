package com.ilayda.jobtracker.repository;

import com.ilayda.jobtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCompanyId(Long companyId);

    @Query("SELECT j FROM JobApplication j JOIN FETCH j.company")
    List<JobApplication> findAllWithCompany();
}
