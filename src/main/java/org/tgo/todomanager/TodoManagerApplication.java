package org.tgo.todomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class TodoManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoManagerApplication.class, args);
	}
}
