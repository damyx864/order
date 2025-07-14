package com.bluedrop.order.domain.event;

import com.bluedrop.order.domain.valueobjects.CustomerId;
import com.bluedrop.order.domain.valueobjects.OrderId;
import lombok.Getter;

@Getter
public class OrderPlacedEvent extends DomainEvent {
    private final OrderId orderId;
    private final CustomerId customerId;

    public OrderPlacedEvent(OrderId orderId, CustomerId customerId) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
    }

}