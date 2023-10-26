package ru.sergdm.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class WsApplication {
	public static void main(String[] args) {
		SpringApplication.run(WsApplication.class, args);
	}
}
