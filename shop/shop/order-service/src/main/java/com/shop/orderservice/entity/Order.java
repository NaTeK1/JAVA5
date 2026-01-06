package com.shop.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// This class represents the "orders" table in the database
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Order creation date and time - required field
    @Column(nullable = false)
    private LocalDateTime date;

    // Order status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING) // Store enum as string in database
    private OrderStatus statut;

    // Total order amount - uses BigDecimal for monetary precision (10 digits, 2 decimals)
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // One-to-Many relationship with OrderLine - cascade all operations and remove orphans
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Prevents infinite recursion during JSON serialization
    private List<OrderLine> orderLines = new ArrayList<>();

    // Automatically set default values before saving to database
    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now(); // Set current date/time
        }
        if (statut == null) {
            statut = OrderStatus.PENDING; // Default status
        }
    }

    // Add an order line and establish bidirectional relationship
    public void addOrderLine(OrderLine orderLine) {
        orderLines.add(orderLine);
        orderLine.setOrder(this);
    }

    // Remove an order line and break bidirectional relationship
    public void removeOrderLine(OrderLine orderLine) {
        orderLines.remove(orderLine);
        orderLine.setOrder(null);
    }

    // Calculate total amount from all order lines (sum of unitPrice * quantity)
    public void calculateTotal() {
        this.totalAmount = orderLines.stream()
                .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
