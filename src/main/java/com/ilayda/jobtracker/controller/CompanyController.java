package com.ilayda.jobtracker.controller;

import com.ilayda.jobtracker.entity.Company;
import com.ilayda.jobtracker.repository.CompanyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyRepository companyRepository;

    @GetMapping
    public List<Company> getAllCompanies(){
        return companyRepository.findAll();
    }

    @PostMapping
    public Company createCompany(@Valid @RequestBody Company company){
        return companyRepository.save(company);
    }
}

