package com.shop.orderservice.controller;

import com.shop.orderservice.dto.OrderDTO;
import com.shop.orderservice.entity.OrderStatus;
import com.shop.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Test class for OrderController - tests REST endpoints
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    // MockMvc - used to simulate HTTP requests to the controller
    @Autowired
    private MockMvc mockMvc;

    // Mock of OrderService - simulates service layer without actual database calls
    @MockBean
    private OrderService orderService;

    // Test data - reused across multiple tests
    private OrderDTO testOrderDTO;

    // Setup method - runs before each test to initialize test data
    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setDate(LocalDateTime.now());
        testOrderDTO.setStatut(OrderStatus.CONFIRMED);
        testOrderDTO.setTotalAmount(BigDecimal.valueOf(1299.99));
    }

    // Test GET /api/orders/{id} - verify order retrieval by ID returns correct data
    @Test
    void testGetOrderById_Success() throws Exception {
        // Mock service to return test order when getOrderById is called
        when(orderService.getOrderById(1L)).thenReturn(testOrderDTO);

        // Perform GET request and verify response
        mockMvc.perform(get("/api/orders/{id}", 1L))
                .andExpect(status().isOk()) // Verify 200 OK status
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(1299.99));
    }

    // Test GET /api/orders - verify retrieval of all orders returns correct list
    @Test
    void testGetAllOrders_Success() throws Exception {
        // Create second test order
        OrderDTO order2 = new OrderDTO();
        order2.setId(2L);
        order2.setStatut(OrderStatus.PENDING);
        order2.setTotalAmount(BigDecimal.valueOf(99.99));

        List<OrderDTO> orders = Arrays.asList(testOrderDTO, order2);

        // Mock service to return list of orders
        when(orderService.getAllOrders()).thenReturn(orders);

        // Perform GET request and verify response contains both orders
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk()) // Verify 200 OK status
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$.length()").value(2)); // Verify 2 orders returned
    }

    // Test POST /api/orders - verify order creation returns 201 Created
    @Test
    void testCreateOrder_Success() throws Exception {
        // Mock service to return created order
        when(orderService.createOrder(any())).thenReturn(testOrderDTO);

        // JSON request body for new order
        String orderJson = """
                {
                    "items": [
                        {
                            "idProduct": 1,
                            "quantity": 1
                        }
                    ]
                }
                """;

        // Perform POST request and verify response
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated()) // Verify 201 Created status
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("CONFIRMED"));
    }

    // Test DELETE /api/orders/{id} - verify order deletion returns 204 No Content
    @Test
    void testDeleteOrder_Success() throws Exception {
        // Perform DELETE request and verify 204 No Content status
        mockMvc.perform(delete("/api/orders/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
