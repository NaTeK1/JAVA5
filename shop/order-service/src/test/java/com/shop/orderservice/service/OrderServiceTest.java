package com.shop.orderservice.service;

import com.shop.orderservice.client.ProductDTO;
import com.shop.orderservice.client.ProductServiceClient;
import com.shop.orderservice.dao.OrderDAO;
import com.shop.orderservice.dto.CreateOrderRequest;
import com.shop.orderservice.dto.OrderDTO;
import com.shop.orderservice.dto.OrderLineRequest;
import com.shop.orderservice.entity.Order;
import com.shop.orderservice.entity.OrderLine;
import com.shop.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // Mock OrderDAO - simulates database access for orders
    @Mock
    private OrderDAO orderDAO;

    // Mock ProductServiceClient - simulates HTTP calls to Product Service
    @Mock
    private ProductServiceClient productServiceClient;

    // OrderService with mocked dependencies injected
    @InjectMocks
    private OrderService orderService;

    // Test data - entities and DTOs reused across tests
    private Order testOrder;
    private ProductDTO testProduct;
    private CreateOrderRequest testCreateRequest;

    // Setup method - runs before each test to initialize test data
    @BeforeEach
    void setUp() {
        testProduct = new ProductDTO();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(BigDecimal.valueOf(1299.99));
        testProduct.setQuantityStock(10);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setDate(LocalDateTime.now());
        testOrder.setStatut(OrderStatus.CONFIRMED);
        testOrder.setTotalAmount(BigDecimal.valueOf(1299.99));

        OrderLine orderLine = new OrderLine();
        orderLine.setId(1L);
        orderLine.setIdProduct(1L);
        orderLine.setQuantity(1);
        orderLine.setUnitPrice(BigDecimal.valueOf(1299.99));
        orderLine.setOrder(testOrder);
        testOrder.getOrderLines().add(orderLine);

        OrderLineRequest lineRequest = new OrderLineRequest(1L, 1);
        testCreateRequest = new CreateOrderRequest(Arrays.asList(lineRequest));
    }

    // Test successful order creation - verify product validation, stock decrease, and order save
    @Test
    void testCreateOrder_Success() {
        // Mock Product Service to return product and successfully decrease stock
        when(productServiceClient.getProduct(1L)).thenReturn(testProduct);
        when(productServiceClient.decreaseStock(1L, 1)).thenReturn(true);
        when(orderDAO.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO result = orderService.createOrder(testCreateRequest);

        // Verify result is correct
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatut());
        assertEquals(BigDecimal.valueOf(1299.99), result.getTotalAmount());
        verify(productServiceClient, times(1)).decreaseStock(1L, 1);
        verify(orderDAO, times(1)).save(any(Order.class));
    }

    // Test order creation fails when product doesn't exist in Product Service
    @Test
    void testCreateOrder_ProductNotFound() {
        // Mock Product Service to return null (product not found)
        when(productServiceClient.getProduct(1L)).thenReturn(null);

        // Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(testCreateRequest);
        });

        // Verify stock was never decreased and order was never saved
        verify(productServiceClient, never()).decreaseStock(anyLong(), anyInt());
        verify(orderDAO, never()).save(any(Order.class));
    }

    // Test order creation fails when product has insufficient stock
    @Test
    void testCreateOrder_InsufficientStock() {
        testProduct.setQuantityStock(0); // No stock available
        when(productServiceClient.getProduct(1L)).thenReturn(testProduct);

        // Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(testCreateRequest);
        });

        // Verify stock decrease was never attempted and order was never saved
        verify(productServiceClient, never()).decreaseStock(anyLong(), anyInt());
        verify(orderDAO, never()).save(any(Order.class));
    }

    // Test order creation fails when Product Service fails to decrease stock
    @Test
    void testCreateOrder_StockDecreaseFailed() {
        // Mock Product Service to return product but fail stock decrease
        when(productServiceClient.getProduct(1L)).thenReturn(testProduct);
        when(productServiceClient.decreaseStock(1L, 1)).thenReturn(false);

        // Verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(testCreateRequest);
        });

        // Verify order was never saved
        verify(orderDAO, never()).save(any(Order.class));
    }

    // Test successful order retrieval by ID with order lines
    @Test
    void testGetOrderById_Success() {
        // Mock DAO to return order with order lines
        when(orderDAO.findByIdWithOrderLines(1L)).thenReturn(testOrder);

        OrderDTO result = orderService.getOrderById(1L);

        // Verify result is correct
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatut());
        verify(orderDAO, times(1)).findByIdWithOrderLines(1L);
    }

    // Test order retrieval fails when order doesn't exist
    @Test
    void testGetOrderById_NotFound() {
        // Mock order not found
        when(orderDAO.findByIdWithOrderLines(1L)).thenReturn(null);

        // Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById(1L);
        });
    }

    // Test successful order status update
    @Test
    void testUpdateOrderStatus_Success() {
        // Mock order exists
        when(orderDAO.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderDAO.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO result = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

        // Verify order was updated
        assertNotNull(result);
        verify(orderDAO, times(1)).save(any(Order.class));
    }

    // Test successful order deletion
    @Test
    void testDeleteOrder_Success() {
        // Mock order exists
        when(orderDAO.existsById(1L)).thenReturn(true);
        doNothing().when(orderDAO).deleteById(1L);

        // Verify no exception is thrown
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));
        verify(orderDAO, times(1)).deleteById(1L);
    }
}
