package com.bluedrop.order.domain.util;

import com.bluedrop.order.application.request.ItemRequest;
import com.bluedrop.order.application.request.PlaceOrderRequest;
import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.OrderStatus;
import com.bluedrop.order.domain.valueobjects.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class OrderTestUtil {

    public static Order getOrder(OrderId orderId, CustomerId customerId) {
        List<Item> items = Arrays.asList(
                new Item(new ProductId(UUID.randomUUID()), "UltraVioletLamp",
                        new Price(new BigDecimal("999.99"), "USD")),
                new Item(new ProductId(UUID.randomUUID()), "WirelessThermometer",
                        new Price(new BigDecimal("29.99"), "USD")),
                new Item(new ProductId(UUID.randomUUID()), "WirelessThermometer",
                        new Price(new BigDecimal("29.99"), "USD"))
        );

        Order order = new Order(orderId, customerId, items);

        assert order.getOrderId().equals(orderId) : "Order ID should match";
        assert order.getCustomerId().equals(customerId) : "Customer ID should match";
        assert order.getStatus() == OrderStatus.PENDING : "New order should be PENDING";
        assert order.getItems().size() == 3 : "Order should have 3 items";
        assert !order.getDomainEvents().isEmpty() : "Order should have domain events";
        return order;
    }

    public static Order createTestOrder() {
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        List<Item> items = List.of(
                new Item(new ProductId(UUID.randomUUID()), "Test Product",
                        new Price(new BigDecimal("100.00"), "USD"))
        );
        return new Order(orderId, customerId, items);
    }

    public static PlaceOrderRequest getPlaceOrderRequest() {
        UUID orderId1 = UUID.randomUUID();
        UUID customerId1 = UUID.randomUUID();
        List<ItemRequest> itemRequests = List.of(
                new ItemRequest(UUID.randomUUID().toString(), "Test Product",
                        new BigDecimal("99.99"), "USD")
        );

        PlaceOrderRequest placeRequest = new PlaceOrderRequest(orderId1.toString(), customerId1.toString(), itemRequests);
        assert placeRequest.getCustomerId().equals(customerId1.toString()) : "PlaceOrderRequest should have correct customer ID";
        return placeRequest;
    }
}
