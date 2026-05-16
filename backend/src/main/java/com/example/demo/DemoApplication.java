package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startklasse der Spring-Boot-Anwendung.
 * Controller, Service und Speicherung sind in eigene Klassen ausgelagert.
 *
 * @author luh
 */
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
