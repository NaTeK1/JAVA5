package com.shop.orderservice.service;

import com.shop.orderservice.client.ProductDTO;
import com.shop.orderservice.client.ProductServiceClient;
import com.shop.orderservice.dao.OrderDAO;
import com.shop.orderservice.dto.*;
import com.shop.orderservice.entity.Order;
import com.shop.orderservice.entity.OrderLine;
import com.shop.orderservice.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// contains business logic and manages transactions
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderDAO orderDAO;
    private final ProductServiceClient productServiceClient; // HTTP client to communicate with Product Service

    // Create a new order - validates products, decreases stock, and saves order
    @Transactional // Ensures all operations succeed or fail together
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("Creating new order");

        Order order = new Order();
        order.setDate(LocalDateTime.now());
        order.setStatut(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process each item in the order
        for (OrderLineRequest item : request.getItems()) {
            // Fetch product details from Product Service
            ProductDTO product = productServiceClient.getProduct(item.getIdProduct());

            if (product == null) {
                throw new IllegalArgumentException("Product not found with ID: " + item.getIdProduct());
            }

            // Verify sufficient stock is available
            if (product.getQuantityStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Decrease stock via Product Service (HTTP call)
            boolean stockDecreased = productServiceClient.decreaseStock(item.getIdProduct(), item.getQuantity());

            if (!stockDecreased) {
                throw new RuntimeException("Failed to decrease stock for product: " + product.getName());
            }

            // Create order line with current product price
            OrderLine orderLine = new OrderLine();
            orderLine.setIdProduct(item.getIdProduct());
            orderLine.setQuantity(item.getQuantity());
            orderLine.setUnitPrice(product.getPrice()); // Capture price at order time
            order.addOrderLine(orderLine);

            // Calculate running total
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        order.setStatut(OrderStatus.CONFIRMED); // Set status to CONFIRMED after successful validation

        Order savedOrder = orderDAO.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return convertToDTO(savedOrder);
    }

    // Retrieve an order by ID with its order lines
    public OrderDTO getOrderById(Long id) {
        log.info("Fetching order with ID: {}", id);
        Order order = orderDAO.findByIdWithOrderLines(id); // Eagerly fetch order lines
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + id);
        }
        return convertToDTO(order);
    }

    // Retrieve all orders from the database
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders");
        return orderDAO.findAll().stream()
                .map(this::convertToDTO) // Convert each Order entity to OrderDTO
                .collect(Collectors.toList());
    }

    // Update the status of an existing order
    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order {} status to {}", id, status);

        Order order = orderDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));

        order.setStatut(status);
        Order updatedOrder = orderDAO.save(order);

        log.info("Order status updated successfully");
        return convertToDTO(updatedOrder);
    }

    // Delete an order by ID (will cascade delete order lines)
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order with ID: {}", id);

        if (!orderDAO.existsById(id)) {
            throw new IllegalArgumentException("Order not found with ID: " + id);
        }

        orderDAO.deleteById(id);
        log.info("Order deleted: {}", id);
    }

    // Convert Order entity to OrderDTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setDate(order.getDate());
        dto.setStatut(order.getStatut());
        dto.setTotalAmount(order.getTotalAmount());

        List<OrderLineDTO> lineDTOs = order.getOrderLines().stream()
                .map(this::convertLineToDTO)
                .collect(Collectors.toList());
        dto.setOrderLines(lineDTOs);

        return dto;
    }

    // Convert OrderLine entity to OrderLineDTO
    private OrderLineDTO convertLineToDTO(OrderLine line) {
        OrderLineDTO dto = new OrderLineDTO();
        dto.setId(line.getId());
        dto.setOrderId(line.getOrder().getId());
        dto.setIdProduct(line.getIdProduct());
        dto.setQuantity(line.getQuantity());
        dto.setUnitPrice(line.getUnitPrice());
        dto.setLineTotal(line.getLineTotal()); // Calculate line total
        return dto;
    }
}
