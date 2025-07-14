package com.bluedrop.order.application.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ItemRequest {
    @NotNull(message = "Product ID cannot be null")
    @NotEmpty(message = "Product ID cannot be empty")
    private String productId;

    @NotNull(message = "Product name cannot be null")
    @NotEmpty(message = "Product name cannot be empty")
    private String productName;

    @NotNull(message = "Unit price cannot be null")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Currency cannot be null")
    @NotEmpty(message = "Currency cannot be empty")
    private String currency;

    @JsonCreator
    public ItemRequest(@JsonProperty("productId") String productId,
                       @JsonProperty("productName") String productName,
                       @JsonProperty("price") BigDecimal price,
                       @JsonProperty("currency") String currency) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.currency = currency;
    }

}
