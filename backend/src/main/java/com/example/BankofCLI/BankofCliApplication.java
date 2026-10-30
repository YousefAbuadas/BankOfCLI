package com.example.BankofCLI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.example")
@EntityScan("com.example.BankofCLI.models")
@EnableJpaRepositories("com.example.BankofCLI.repositories")
public class BankofCliApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankofCliApplication.class, args);
	}

}
