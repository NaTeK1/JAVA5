package com.shop.productservice.controller;

import com.shop.productservice.dto.ProductDTO;
import com.shop.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Test class for ProductController - tests REST endpoints without starting full application
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    // MockMvc - used to simulate HTTP requests to the controller
    @Autowired
    private MockMvc mockMvc;

    // Mock of ProductService - simulates service layer without actual database calls
    @MockBean
    private ProductService productService;

    // Test data - reused across multiple tests
    private ProductDTO testProductDTO;

    // Setup method - runs before each test to initialize test data
    @BeforeEach
    void setUp() {
        testProductDTO = new ProductDTO();
        testProductDTO.setId(1L);
        testProductDTO.setName("Laptop");
        testProductDTO.setDescription("Gaming laptop");
        testProductDTO.setPrice(BigDecimal.valueOf(1299.99));
        testProductDTO.setQuantityStock(10);
        testProductDTO.setCategoryId(1L);
        testProductDTO.setCategoryName("Electronics");
    }

    // Test GET /api/products/{id} - verify product retrieval by ID returns correct data
    @Test
    void testGetProductById_Success() throws Exception {
        // Mock service to return test product when getProductById is called
        when(productService.getProductById(1L)).thenReturn(testProductDTO);

        // Perform GET request and verify response
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk()) // Verify 200 OK status
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.quantityStock").value(10));
    }

    // Test GET /api/products - verify retrieval of all products returns correct list
    @Test
    void testGetAllProducts_Success() throws Exception {
        // Create second test product
        ProductDTO product2 = new ProductDTO();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setPrice(BigDecimal.valueOf(29.99));
        product2.setQuantityStock(50);
        product2.setCategoryId(1L);

        List<ProductDTO> products = Arrays.asList(testProductDTO, product2);

        // Mock service to return list of products
        when(productService.getAllProducts()).thenReturn(products);

        // Perform GET request and verify response contains both products
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk()) // Verify 200 OK status
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$.length()").value(2)); // Verify 2 products returned
    }

    // Test POST /api/products - verify product creation returns 201 Created
    @Test
    void testCreateProduct_Success() throws Exception {
        // Mock service to return created product
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(testProductDTO);

        // JSON request body for new product
        String productJson = """
                {
                    "name": "Laptop",
                    "description": "Gaming laptop",
                    "price": 1299.99,
                    "quantityStock": 10,
                    "categoryId": 1
                }
                """;

        // Perform POST request and verify response
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated()) // Verify 201 Created status
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1299.99));
    }

    // Test DELETE /api/products/{id} - verify product deletion returns 204 No Content
    @Test
    void testDeleteProduct_Success() throws Exception {
        // Perform DELETE request and verify 204 No Content status
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
