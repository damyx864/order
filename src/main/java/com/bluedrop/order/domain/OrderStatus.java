package com.bluedrop.order.domain;

public enum OrderStatus {
    PENDING, APPROVED, CANCELLED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == APPROVED || newStatus == CANCELLED;
            case APPROVED, CANCELLED -> false;
        };
    }

    public boolean canNotTransitionTo(OrderStatus newStatus) {
        return !canTransitionTo(newStatus);
    }
}
