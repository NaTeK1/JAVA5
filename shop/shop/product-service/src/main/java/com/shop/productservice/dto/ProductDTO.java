package com.shop.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    // Product name - required field with max 200 characters
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    // Product description - optional field with max 1000 characters
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // Product price - required field, must be greater than 0 (uses BigDecimal for precision)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    // Stock quantity - required field, cannot be negative
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer quantityStock;

    // Category ID - required field to associate product with a category
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Category name - read-only field populated when retrieving products (not validated)
    private String categoryName;
}
