package com.example.gmailapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailrestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailrestApplication.class, args);
		try {
			Demo.ejecutar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
