package com.ilayda.jobtracker.repository;

import com.ilayda.jobtracker.entity.Company;
import com.ilayda.jobtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}