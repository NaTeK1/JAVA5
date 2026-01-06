package com.shop.productservice.dao;

import com.shop.productservice.entity.Product;
import com.shop.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductDAO {

    private final ProductRepository productRepository;

    // Save or update a product in the database
    public Product save(Product product) {
        return productRepository.save(product);
    }

    // Find a product by its ID
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    // Retrieve all products from the database
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // Find all products belonging to a specific category
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Search products by name
    public List<Product> findByNameContaining(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Find products with stock quantity below a given threshold
    public List<Product> findLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    // Delete a product by its ID
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // Check if a product exists by its ID
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    // Decrease product stock by a given quantity (atomic operation)
    @Transactional
    public boolean decreaseStock(Long productId, Integer quantity) {
        int updated = productRepository.decreaseStock(productId, quantity);
        return updated > 0;
    }
}
