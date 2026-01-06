package com.shop.orderservice.dao;

import com.shop.orderservice.entity.Order;
import com.shop.orderservice.entity.OrderStatus;
import com.shop.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderDAO {

    private final OrderRepository orderRepository;

    // Save or update an order in the database
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    // Find an order by ID (returns Optional)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    // Find an order by ID with its order lines eagerly loaded (JOIN FETCH)
    public Order findByIdWithOrderLines(Long id) {
        return orderRepository.findByIdWithOrderLines(id);
    }

    // Retrieve all orders from the database
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    // Find all orders with a specific status (PENDING, CONFIRMED, SHIPPED, etc.)
    public List<Order> findByStatut(OrderStatus statut) {
        return orderRepository.findByStatut(statut);
    }

    // Find all orders created within a date range
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }

    // Delete an order by ID
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    // Check if an order exists by ID
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }
}
