package com.bluedrop.order.domain.event;

import com.bluedrop.order.domain.valueobjects.OrderId;
import lombok.Getter;

@Getter
public class OrderApprovedEvent extends DomainEvent {
    private final OrderId orderId;

    public OrderApprovedEvent(OrderId orderId) {
        super();
        this.orderId = orderId;
    }

}