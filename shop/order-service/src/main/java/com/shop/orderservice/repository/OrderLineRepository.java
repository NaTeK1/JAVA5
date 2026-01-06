package com.shop.orderservice.repository;

import com.shop.orderservice.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    // Find all order lines belonging to a specific order
    List<OrderLine> findByOrderId(Long orderId);

    // Find all order lines containing a specific product
    List<OrderLine> findByIdProduct(Long idProduct);

    // Find a specific order line by order ID and product ID
    @Query("SELECT ol FROM OrderLine ol WHERE ol.order.id = :orderId AND ol.idProduct = :productId")
    OrderLine findByOrderIdAndProductId(@Param("orderId") Long orderId,
                                       @Param("productId") Long productId);
}
