package com.bluedrop.order.application.response;

import com.bluedrop.order.domain.valueobjects.Item;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ItemResponse {
    private String productId;
    private String productName;
    private BigDecimal price;
    private String currency;

    public ItemResponse(Item item) {
        this.productId = String.valueOf(item.productId().id());
        this.productName = item.productName();
        this.price = item.price().amount();
        this.currency = item.price().currency();
    }
}