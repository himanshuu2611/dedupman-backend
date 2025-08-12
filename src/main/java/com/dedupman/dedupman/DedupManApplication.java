package com.dedupman.dedupman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class DedupManApplication {
	public static void main(String[] args) {
		SpringApplication.run(DedupManApplication.class, args);
	}
}
