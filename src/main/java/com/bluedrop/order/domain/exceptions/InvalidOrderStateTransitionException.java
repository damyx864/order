package com.bluedrop.order.domain.exceptions;

import com.bluedrop.order.domain.OrderStatus;

public class InvalidOrderStateTransitionException extends OrderBusinessException {
    public InvalidOrderStateTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format("Cannot transition order from %s to %s", from, to));
    }
}

