package com.ashok.kalaha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class KalahaApplication {
  public static void main(String[] args) {
    SpringApplication.run(KalahaApplication.class, args);
  }
}
