package com.shop.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// used when order-service needs to decrease stock
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    // Product ID - identifies which product's stock to decrease
    @NotNull(message = "Product ID is required")
    private Long productId;

    // Quantity to decrease - must be at least 1
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
