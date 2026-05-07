package com.bluedrop.order.domain.repository;

import com.bluedrop.order.domain.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile({"prod"})
public interface JpaOrderRepository extends OrderRepository {

//    Order save(Order order);
//    Optional<Order> findById(UUID orderId);
    List<Order> findByCustomerId(UUID customerId);
}