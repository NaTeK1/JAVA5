package com.shop.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineDTO {

    private Long id;

    // Reference to the parent order
    private Long orderId;

    @NotNull(message = "Product ID is required")
    private Long idProduct;

    // Quantity ordered
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Unit price at the time of order uses BigDecimal for monetary precision
    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    // Total for this line (unitPrice * quantity) - calculated field
    private BigDecimal lineTotal;
}
