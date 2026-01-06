package com.shop.productservice.service;

import com.shop.productservice.dao.CategoryDAO;
import com.shop.productservice.dao.ProductDAO;
import com.shop.productservice.dto.ProductDTO;
import com.shop.productservice.dto.StockUpdateRequest;
import com.shop.productservice.entity.Category;
import com.shop.productservice.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    // DAO layer for product database access
    private final ProductDAO productDAO;
    // DAO layer for category database access
    private final CategoryDAO categoryDAO;

    // Create a new product - validates category exists before creation
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Verify that the category exists
        Category category = categoryDAO.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + productDTO.getCategoryId()));

        // Convert DTO to Entity
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantityStock(productDTO.getQuantityStock());
        product.setCategory(category);

        // Save to database
        Product savedProduct = productDAO.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        // Convert Entity back to DTO and return
        return convertToDTO(savedProduct);
    }

    // Retrieve a single product by ID - throws exception if not found
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        return convertToDTO(product);
    }

    // Retrieve all products from the database
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        return productDAO.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Retrieve all products belonging to a specific category - validates category exists
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.info("Fetching products for category ID: {}", categoryId);

        // Verify that the category exists
        if (!categoryDAO.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found with ID: " + categoryId);
        }

        return productDAO.findByCategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Update an existing product - validates both product and category exist
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        // Find the product to update
        Product product = productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // Verify that the new category exists
        Category category = categoryDAO.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + productDTO.getCategoryId()));

        // Update product fields
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantityStock(productDTO.getQuantityStock());
        product.setCategory(category);

        // Save changes to database
        Product updatedProduct = productDAO.save(product);
        log.info("Product updated: {}", updatedProduct.getId());

        return convertToDTO(updatedProduct);
    }

    // Delete a product by ID - throws exception if not found
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        // Check if product exists before attempting delete
        if (!productDAO.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        productDAO.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    // Decrease product stock - validates sufficient quantity is available
    @Transactional
    public boolean decreaseStock(StockUpdateRequest request) {
        log.info("Decreasing stock for product ID: {} by quantity: {}", request.getProductId(), request.getQuantity());

        // Find the product
        Product product = productDAO.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + request.getProductId()));

        // Check if sufficient stock is available
        if (product.getQuantityStock() < request.getQuantity()) {
            log.warn("Insufficient stock for product ID: {}. Available: {}, Requested: {}",
                    request.getProductId(), product.getQuantityStock(), request.getQuantity());
            return false;
        }

        // Perform atomic stock decrease
        boolean success = productDAO.decreaseStock(request.getProductId(), request.getQuantity());
        if (success) {
            log.info("Stock decreased successfully for product ID: {}", request.getProductId());
        }
        return success;
    }

    // Convert Product entity to ProductDTO for API responses (includes category info)
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantityStock(product.getQuantityStock());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }
}
