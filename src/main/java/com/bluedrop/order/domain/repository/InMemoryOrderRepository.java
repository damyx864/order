package com.bluedrop.order.domain.repository;

import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.valueobjects.OrderId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// In-Memory Repository Implementation for Testing
@Profile("dev")
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<OrderId, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        return orders.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(orders.get(new OrderId(orderId)));
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return orders.values().stream()
                .filter(order -> order.getCustomerId().id().equals(customerId))
                .collect(Collectors.toList());
    }
}
