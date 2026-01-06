package com.shop.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// used in CreateOrderRequest (contains only product ID and quantity)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineRequest {

    // Product ID to order
    @NotNull(message = "Product ID is required")
    private Long idProduct;

    // Quantity to order
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
