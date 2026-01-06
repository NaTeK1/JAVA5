package com.shop.orderservice.repository;

import com.shop.orderservice.entity.Order;
import com.shop.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find all orders with a specific status (PENDING, CONFIRMED, SHIPPED, etc.)
    List<Order> findByStatut(OrderStatus statut);

    // Find all orders created within a date range
    @Query("SELECT o FROM Order o WHERE o.date BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    // Find order by ID and eagerly fetch its order lines
    @Query("SELECT o FROM Order o JOIN FETCH o.orderLines WHERE o.id = :orderId")
    Order findByIdWithOrderLines(@Param("orderId") Long orderId);
}
