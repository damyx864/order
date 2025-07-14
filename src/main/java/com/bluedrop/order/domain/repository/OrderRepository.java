package com.bluedrop.order.domain.repository;

import com.bluedrop.order.domain.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Domain Repository
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    List<Order> findByCustomerId(UUID customerId);
}
