package com.shop.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main entry point for the Product Service microservice
// @SpringBootApplication enables auto-configuration, component scanning, and configuration properties
@SpringBootApplication
public class ProductServiceApplication {

    // Main method - starts the Spring Boot application
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
