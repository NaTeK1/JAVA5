package com.shop.orderservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// DTO for Product data received from Product Service via HTTP
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    // Product unique identifier
    private Long id;

    // Product name
    private String name;

    // Product description
    private String description;

    // Product price - uses BigDecimal for monetary precision
    private BigDecimal price;

    // Available stock quantity
    private Integer quantityStock;

    // Category ID the product belongs to
    private Long categoryId;

    // Category name (included for convenience)
    private String categoryName;
}
