package com.bluedrop.order.config;

import com.bluedrop.order.domain.repository.InMemoryOrderRepository;
import com.bluedrop.order.domain.repository.OrderRepository;
import com.bluedrop.order.domain.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OrderConfiguration {

    @Bean
    public OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }

    @Bean
    public OrderService orderService(OrderRepository orderRepository) {
        return new OrderService(orderRepository);
    }
}
