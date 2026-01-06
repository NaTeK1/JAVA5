package com.shop.orderservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

// HTTP client to communicate with Product Service
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final WebClient.Builder webClientBuilder; // WebClient for making HTTP requests

    @Value("${product.service.url}") // Injected from application.properties (http://localhost:8081)
    private String productServiceUrl;

    // Fetch product details from Product Service via HTTP GET request
    public ProductDTO getProduct(Long productId) {
        log.info("Fetching product {} from product-service", productId);

        try {
            return webClientBuilder.build()
                    .get() // HTTP GET method
                    .uri(productServiceUrl + "/api/products/{id}", productId) // Build URL with path variable
                    .retrieve() // Execute request
                    .bodyToMono(ProductDTO.class) // Convert response to ProductDTO
                    .block(); // Block and wait for response (synchronous call)
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product from product-service", e);
        }
    }

    // Request stock decrease from Product Service via HTTP POST request
    public boolean decreaseStock(Long productId, Integer quantity) {
        log.info("Requesting stock decrease for product {} by quantity {}", productId, quantity);

        try {
            // Prepare request body
            Map<String, Object> request = new HashMap<>();
            request.put("productId", productId);
            request.put("quantity", quantity);

            Boolean result = webClientBuilder.build()
                    .post() // HTTP POST method
                    .uri(productServiceUrl + "/api/products/decrease-stock") // Endpoint URL
                    .bodyValue(request) // Send request body as JSON
                    .retrieve() // Execute request
                    .bodyToMono(Boolean.class) // Convert response to Boolean
                    .onErrorResume(e -> { // Handle errors gracefully
                        log.error("Error decreasing stock for product {}: {}", productId, e.getMessage());
                        return Mono.just(false); // Return false on error
                    })
                    .block(); // Block and wait for response (synchronous call)

            log.info("Stock decrease result for product {}: {}", productId, result);
            return Boolean.TRUE.equals(result); // Return true only if result is explicitly true
        } catch (Exception e) {
            log.error("Error communicating with product-service: {}", e.getMessage());
            return false; // Return false on communication error
        }
    }
}
