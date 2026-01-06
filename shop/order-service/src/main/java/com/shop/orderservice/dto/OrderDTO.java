package com.shop.orderservice.dto;

import com.shop.orderservice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    private LocalDateTime date;

    private OrderStatus statut;

    private BigDecimal totalAmount;

    private List<OrderLineDTO> orderLines = new ArrayList<>();
}
