package com.shop.productservice.controller;

import com.shop.productservice.dto.ProductDTO;
import com.shop.productservice.dto.StockUpdateRequest;
import com.shop.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // POST /api/products - Create a new product (validates request body)
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED); // Returns 201 Created
    }

    // GET /api/products/{id} - Retrieve a single product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product); // Returns 200 OK
    }

    // GET /api/products - Retrieve all products
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // Returns 200 OK
    }

    // GET /api/products/category/{categoryId} - Retrieve all products in a specific category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products); // Returns 200 OK
    }

    // PUT /api/products/{id} - Update an existing product (validates request body)
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updated); // Returns 200 OK
    }

    // DELETE /api/products/{id} - Delete a product by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }

    // POST /api/products/decrease-stock - Decrease product stock (used by order-service)
    @PostMapping("/decrease-stock")
    public ResponseEntity<Boolean> decreaseStock(@Valid @RequestBody StockUpdateRequest request) {
        boolean success = productService.decreaseStock(request);
        if (success) {
            return ResponseEntity.ok(true); // Returns 200 OK if stock decreased
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false); // Returns 400 Bad Request if insufficient stock
        }
    }
}
