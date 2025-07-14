package com.bluedrop.order.application.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PlaceOrderRequest {
    // For testing purposes only
    // see the file rest-api.http
    @NotNull(message = "Order ID cannot be null")
    @NotEmpty(message = "Order ID cannot be empty")
    private String orderId;

    @NotNull(message = "Customer ID cannot be null")
    @NotEmpty(message = "Customer ID cannot be empty")
    private String customerId;

    @NotNull(message = "Items cannot be null")
    @NotEmpty(message = "Order must have at least one item")
    private List<@Valid ItemRequest> items;

    @JsonCreator
    public PlaceOrderRequest(@JsonProperty("orderId") String orderId,
                             @JsonProperty("customerId") String customerId,
                             @JsonProperty("items") List<ItemRequest> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
    }

}