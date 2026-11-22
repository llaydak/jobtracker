package com.ilayda.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class JobtrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobtrackerApplication.class, args);
	}

}
