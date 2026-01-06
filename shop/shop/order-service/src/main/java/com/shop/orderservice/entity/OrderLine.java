package com.shop.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    @JsonBackReference // Prevents infinite recursion during JSON serialization (counterpart of @JsonManagedReference)
    private Order order;

    // Product ID reference - stored as Long (no direct JPA relationship due to microservices architecture)
    @Column(name = "id_product", nullable = false)
    private Long idProduct;

    // Quantity of the product ordered - required field
    @Column(nullable = false)
    private Integer quantity;

    // Unit price at the time of order - uses BigDecimal for monetary precision (10 digits, 2 decimals)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Calculate the total for this line (unitPrice * quantity)
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
