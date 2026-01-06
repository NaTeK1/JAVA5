package com.shop.orderservice.entity;

// Enum representing the different states of an order throughout its lifecycle
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
