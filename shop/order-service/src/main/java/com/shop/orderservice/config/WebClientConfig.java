package com.shop.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

//used for HTTP communication with Product Service
@Configuration
public class WebClientConfig {

    // Creates a WebClient.Builder bean for making HTTP requests to external services
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder(); // Returns a WebClient builder that can be injected and customized
    }
}
