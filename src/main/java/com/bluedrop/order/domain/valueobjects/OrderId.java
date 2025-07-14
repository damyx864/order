package com.bluedrop.order.domain.valueobjects;

import org.springframework.util.Assert;

import java.util.UUID;

// OrderId Value Object
public record OrderId(UUID id) {
    public OrderId {
        Assert.notNull(id, "id must not be null");
    }

    public OrderId() {
        this(UUID.randomUUID());
    }
}