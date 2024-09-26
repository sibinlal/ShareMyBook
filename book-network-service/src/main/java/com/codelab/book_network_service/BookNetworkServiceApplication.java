package com.codelab.book_network_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BookNetworkServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookNetworkServiceApplication.class, args);
	}

}
