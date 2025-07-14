package com.bluedrop.order.domain.valueobjects;

public record Item(ProductId productId, String productName, Price price) {
    public Item {
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
    }
}