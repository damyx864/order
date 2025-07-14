package com.bluedrop.order.domain.exceptions;

public class OrderBusinessException extends RuntimeException {
    public OrderBusinessException(String message) {
        super(message);
    }
}