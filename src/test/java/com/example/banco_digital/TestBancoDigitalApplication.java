package com.example.banco_digital;

import org.springframework.boot.SpringApplication;

public class TestBancoDigitalApplication {

	public static void main(String[] args) {
		SpringApplication.from(BancoDigitalApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
