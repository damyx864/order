package com.bluedrop.order.domain;

import com.bluedrop.order.domain.event.DomainEvent;
import com.bluedrop.order.domain.event.OrderApprovedEvent;
import com.bluedrop.order.domain.event.OrderCancelledEvent;
import com.bluedrop.order.domain.event.OrderPlacedEvent;
import com.bluedrop.order.domain.exceptions.InvalidOrderStateTransitionException;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import com.bluedrop.order.domain.valueobjects.CustomerId;
import com.bluedrop.order.domain.valueobjects.Item;
import com.bluedrop.order.domain.valueobjects.OrderId;
import com.bluedrop.order.domain.valueobjects.Price;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Order Aggregate Root
@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class Order {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final LocalDateTime createdAt;
    private OrderStatus status;
    private final List<Item> items;
    private final List<DomainEvent> domainEvents;

    public Order(OrderId orderId, CustomerId customerId, List<Item> items) {
        if (orderId == null) {
            throw new OrderBusinessException("OrderId cannot be null");
        }
        if (customerId == null) {
            throw new OrderBusinessException("CustomerId cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new OrderBusinessException("Order must have at least one item");
        }

        this.orderId = orderId;
        this.customerId = customerId;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.domainEvents = new ArrayList<>();
        // initial state of a new order
        this.status = OrderStatus.PENDING;


        // Raise domain event
        addDomainEvent(new OrderPlacedEvent(orderId, customerId));
    }

    public void approve() {
        if (status.canNotTransitionTo(OrderStatus.APPROVED)) {
            throw new InvalidOrderStateTransitionException(status, OrderStatus.APPROVED);
        }

        this.status = OrderStatus.APPROVED;
        addDomainEvent(new OrderApprovedEvent(orderId));
    }

    public void cancel() {
        if (status.canNotTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidOrderStateTransitionException(status, OrderStatus.CANCELLED);
        }

        this.status = OrderStatus.CANCELLED;
        addDomainEvent(new OrderCancelledEvent(orderId));
    }

    public Price getTotalAmount() {
        return items.stream()
                .map(Item::price)
                .reduce(Price::add)
                .orElse(new Price(BigDecimal.ZERO, "USD"));
    }

    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
