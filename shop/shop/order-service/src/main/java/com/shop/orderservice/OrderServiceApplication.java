package com.shop.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main entry point for the Order Service microservice
@SpringBootApplication
public class OrderServiceApplication {

    // Main method - starts the Spring Boot application
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
