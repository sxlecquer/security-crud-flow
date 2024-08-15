package com.example.client;

import com.example.client.service.CuratorService;
import com.example.client.service.LecturerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(CuratorService curatorService, LecturerService lecturerService) {
		return args -> {
			if(curatorService.findAll().isEmpty()) curatorService.fillCuratorTable();
			if(lecturerService.findAll().isEmpty()) lecturerService.fillLecturerTable();
		};
	}
}
