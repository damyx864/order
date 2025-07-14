package com.bluedrop.order.domain.valueobjects;

import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import org.springframework.util.Assert;

import java.math.BigDecimal;

// Price Value Object
public record Price(BigDecimal amount, String currency) {
    public Price {
        Assert.notNull(amount, "Amount cannot be null");
        Assert.notNull(currency, "Currency cannot be null");
        // more validation
    }

    public Price multiply(int quantity) {
        return new Price(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Price add(Price other) {
        if (!currency.equals(other.currency)) {
            throw new OrderBusinessException("Cannot add different currencies");
        }
        return new Price(amount.add(other.amount), currency);
    }
}