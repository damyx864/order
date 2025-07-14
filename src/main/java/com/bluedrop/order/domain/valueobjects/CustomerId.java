package com.bluedrop.order.domain.valueobjects;

import org.springframework.util.Assert;

import java.util.UUID;

// CustomerId Value Object
public record CustomerId(UUID id) {
    public CustomerId {
        Assert.notNull(id, "id must not be null");
    }

    public CustomerId() {
        this(UUID.randomUUID());
    }

}
