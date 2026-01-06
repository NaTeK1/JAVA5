package com.shop.orderservice.dao;

import com.shop.orderservice.entity.OrderLine;
import com.shop.orderservice.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderLineDAO {

    private final OrderLineRepository orderLineRepository;

    // Save or update an order line in the database
    public OrderLine save(OrderLine orderLine) {
        return orderLineRepository.save(orderLine);
    }

    // Find an order line by ID (returns Optional)
    public Optional<OrderLine> findById(Long id) {
        return orderLineRepository.findById(id);
    }

    // Retrieve all order lines from the database
    public List<OrderLine> findAll() {
        return orderLineRepository.findAll();
    }

    // Find all order lines belonging to a specific order
    public List<OrderLine> findByOrderId(Long orderId) {
        return orderLineRepository.findByOrderId(orderId);
    }

    // Find all order lines containing a specific product (useful for product history)
    public List<OrderLine> findByIdProduct(Long idProduct) {
        return orderLineRepository.findByIdProduct(idProduct);
    }

    // Find a specific order line by order ID and product ID
    public OrderLine findByOrderIdAndProductId(Long orderId, Long productId) {
        return orderLineRepository.findByOrderIdAndProductId(orderId, productId);
    }

    // Delete an order line by ID
    public void deleteById(Long id) {
        orderLineRepository.deleteById(id);
    }

    // Check if an order line exists by ID
    public boolean existsById(Long id) {
        return orderLineRepository.existsById(id);
    }
}
