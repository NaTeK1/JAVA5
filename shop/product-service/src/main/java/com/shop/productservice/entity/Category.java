package com.shop.productservice.entity;

// Handles JSON serialization to avoid infinite loops
import com.fasterxml.jackson.annotation.JsonManagedReference;
//la norme pour mapper des objets Java -> tables SQL
import jakarta.persistence.*;

//library that automatically generates repetitive code
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

//this class represents a table in the database
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // List of products in this category (One-to-Many relationship)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Product> products = new ArrayList<>();

    // Add a product to this category (maintains both sides of relationship)
    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    // Remove a product from this category (maintains both sides of relationship)
    public void removeProduct(Product product) {
        products.remove(product);
        product.setCategory(null);
    }
}
