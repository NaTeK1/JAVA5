package com.shop.productservice.repository;

import com.shop.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all products belonging to a specific category
    List<Product> findByCategoryId(Long categoryId);

    // Search products by name
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find products with stock quantity below a given threshold
    @Query("SELECT p FROM Product p WHERE p.quantityStock < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // Decrease product stock by a given quantity (only if sufficient stock available)
    @Modifying
    @Query("UPDATE Product p SET p.quantityStock = p.quantityStock - :quantity WHERE p.id = :productId AND p.quantityStock >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
