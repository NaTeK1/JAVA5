package com.shop.productservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Quantity available in stock
    @Column(name = "quantity_stock", nullable = false)
    private Integer quantityStock;

    // Reference to the category (Many-to-One relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", nullable = false)
    @JsonBackReference
    private Category category;
}
