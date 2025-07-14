package com.bluedrop.order.domain.event;

import com.bluedrop.order.domain.valueobjects.OrderId;
import lombok.Getter;

@Getter
public class OrderCancelledEvent extends DomainEvent {
    private final OrderId orderId;

    public OrderCancelledEvent(OrderId orderId) {
        super();
        this.orderId = orderId;
    }

}
