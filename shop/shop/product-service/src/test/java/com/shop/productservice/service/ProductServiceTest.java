package com.shop.productservice.service;

import com.shop.productservice.dao.CategoryDAO;
import com.shop.productservice.dao.ProductDAO;
import com.shop.productservice.dto.ProductDTO;
import com.shop.productservice.dto.StockUpdateRequest;
import com.shop.productservice.entity.Category;
import com.shop.productservice.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // Mock ProductDAO - simulates database access for products
    @Mock
    private ProductDAO productDAO;

    // Mock CategoryDAO - simulates database access for categories
    @Mock
    private CategoryDAO categoryDAO;

    // ProductService with mocked DAOs injected
    @InjectMocks
    private ProductService productService;

    // Test data - entities and DTOs reused across tests
    private Category testCategory;
    private Product testProduct;
    private ProductDTO testProductDTO;

    // Setup method - runs before each test to initialize test data
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic devices");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setDescription("Gaming laptop");
        testProduct.setPrice(BigDecimal.valueOf(1299.99));
        testProduct.setQuantityStock(10);
        testProduct.setCategory(testCategory);

        testProductDTO = new ProductDTO();
        testProductDTO.setId(1L);
        testProductDTO.setName("Laptop");
        testProductDTO.setDescription("Gaming laptop");
        testProductDTO.setPrice(BigDecimal.valueOf(1299.99));
        testProductDTO.setQuantityStock(10);
        testProductDTO.setCategoryId(1L);
    }

    // Test successful product creation - verify product is saved and DTO is returned
    @Test
    void testCreateProduct_Success() {
        // Mock DAOs to return category and saved product
        when(categoryDAO.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productDAO.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO result = productService.createProduct(testProductDTO);

        // Verify result is correct
        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        assertEquals(BigDecimal.valueOf(1299.99), result.getPrice());
        verify(productDAO, times(1)).save(any(Product.class));
    }

    // Test product creation fails when category doesn't exist
    @Test
    void testCreateProduct_CategoryNotFound() {
        // Mock category not found
        when(categoryDAO.findById(1L)).thenReturn(Optional.empty());

        // Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(testProductDTO);
        });

        // Verify product was never saved
        verify(productDAO, never()).save(any(Product.class));
    }

    // Test successful product retrieval by ID
    @Test
    void testGetProductById_Success() {
        // Mock DAO to return product
        when(productDAO.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDTO result = productService.getProductById(1L);

        // Verify result is correct
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        verify(productDAO, times(1)).findById(1L);
    }

    // Test product retrieval fails when product doesn't exist
    @Test
    void testGetProductById_NotFound() {
        // Mock product not found
        when(productDAO.findById(1L)).thenReturn(Optional.empty());

        // Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductById(1L);
        });
    }

    // Test successful stock decrease when sufficient stock available
    @Test
    void testDecreaseStock_Success() {
        StockUpdateRequest request = new StockUpdateRequest(1L, 5);

        // Mock DAO to return product and successful stock decrease
        when(productDAO.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productDAO.decreaseStock(1L, 5)).thenReturn(true);

        boolean result = productService.decreaseStock(request);

        // Verify stock was decreased
        assertTrue(result);
        verify(productDAO, times(1)).decreaseStock(1L, 5);
    }

    // Test stock decrease fails when insufficient stock available
    @Test
    void testDecreaseStock_InsufficientStock() {
        testProduct.setQuantityStock(3); // Only 3 in stock
        StockUpdateRequest request = new StockUpdateRequest(1L, 5); // Requesting 5

        // Mock DAO to return product with low stock
        when(productDAO.findById(1L)).thenReturn(Optional.of(testProduct));

        boolean result = productService.decreaseStock(request);

        // Verify stock decrease was not attempted
        assertFalse(result);
        verify(productDAO, never()).decreaseStock(anyLong(), anyInt());
    }

    // Test successful product deletion
    @Test
    void testDeleteProduct_Success() {
        // Mock product exists
        when(productDAO.existsById(1L)).thenReturn(true);
        doNothing().when(productDAO).deleteById(1L);

        // Verify no exception is thrown
        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productDAO, times(1)).deleteById(1L);
    }

    // Test product deletion fails when product doesn't exist
    @Test
    void testDeleteProduct_NotFound() {
        // Mock product not found
        when(productDAO.existsById(1L)).thenReturn(false);

        // Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProduct(1L);
        });

        // Verify delete was never called
        verify(productDAO, never()).deleteById(anyLong());
    }
}
