package com.bluedrop.order.domain.valueobjects;

import org.springframework.util.Assert;

import java.util.UUID;

// ProductId Value Object
public record ProductId(UUID id) {
    public ProductId {
        Assert.notNull(id, "id must not be null");
    }

    public ProductId() {
        this(UUID.randomUUID());
    }
}
