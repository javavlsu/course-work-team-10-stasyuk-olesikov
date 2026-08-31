package ru.vlsu.myng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:credentials.properties")
@Configuration
@SpringBootApplication
public class MyNgApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyNgApplication.class, args);
	}

}
