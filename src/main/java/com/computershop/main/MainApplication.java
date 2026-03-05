package com.computershop.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {

	public static void main(String[] args) {
		System.out.println("Starting Computer Shop Application...");
		SpringApplication.run(MainApplication.class, args);
		System.out.println("Computer Shop Application Started Successfully.");
		System.out.println("Access the application at http://localhost:8080");
		System.out.println("Use default admin credentials - Username: admin | Password: 123456");
	}

}
