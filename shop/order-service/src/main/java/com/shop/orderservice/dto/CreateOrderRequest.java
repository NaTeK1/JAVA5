package com.shop.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO for creating a new order - received from client in POST /api/orders request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    // List of items to include in the order
    @NotEmpty(message = "Order must contain at least one item")
    @Valid // Validates each OrderLineRequest item in the list
    private List<OrderLineRequest> items;
}
