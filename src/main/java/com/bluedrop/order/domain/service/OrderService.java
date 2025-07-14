package com.bluedrop.order.domain.service;

import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import com.bluedrop.order.domain.repository.OrderRepository;
import com.bluedrop.order.domain.valueobjects.CustomerId;
import com.bluedrop.order.domain.valueobjects.Item;
import com.bluedrop.order.domain.valueobjects.OrderId;
import org.springframework.stereotype.Service;

import java.util.List;

// Domain Service
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(OrderId orderId, CustomerId customerId, List<Item> items) {
        // Check if the order already exists
        if (orderRepository.findById(orderId.id()).isPresent()) {
            throw new OrderBusinessException("Order with ID " + orderId + " already exists");
        }

        Order order = new Order(orderId, customerId, items);
        orderRepository.save(order);
        return order;
    }

    public Order approveOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId.id())
                .orElseThrow(() -> new OrderBusinessException("Order not found: " + orderId));

        order.approve();
        return orderRepository.save(order);
    }

    public Order cancelOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId.id())
                .orElseThrow(() -> new OrderBusinessException("Order not found: " + orderId));

        order.cancel();
        return orderRepository.save(order);
    }

    public Order findOrder(OrderId orderId) {
        return orderRepository.findById(orderId.id())
                .orElseThrow(() -> new OrderBusinessException("Order not found: " + orderId));
    }

    public List<Order> findOrdersByCustomer(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId.id());
    }
}
