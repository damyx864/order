package com.bluedrop.order.application.controller;

import com.bluedrop.order.application.request.PlaceOrderRequest;
import com.bluedrop.order.application.response.OrderResponse;
import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.exceptions.InvalidOrderStateTransitionException;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import com.bluedrop.order.domain.service.OrderService;
import com.bluedrop.order.domain.valueobjects.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        try {

            // should be moved to a mapper
            List<Item> items = request.getItems().stream()
                    .map(itemRequest -> new Item(
                            new ProductId(UUID.fromString(itemRequest.getProductId())),
                            itemRequest.getProductName(),
                            new Price(itemRequest.getPrice(), itemRequest.getCurrency())
                    ))
                    .collect(Collectors.toList());

            CustomerId customerId = new CustomerId(UUID.fromString(request.getCustomerId()));
            OrderId orderId = new OrderId(UUID.fromString(request.getOrderId()));
            Order order = orderService.placeOrder(orderId, customerId, items);
            OrderResponse response = new OrderResponse(order);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (OrderBusinessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PutMapping(value = "/{orderId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> approveOrder(@PathVariable UUID orderId) {
        try {
            return ResponseEntity.ok(new OrderResponse(orderService.approveOrder(new OrderId(orderId))));

        } catch (InvalidOrderStateTransitionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        } catch (OrderBusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PutMapping(value = "/{orderId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        try {
            return ResponseEntity.ok(new OrderResponse(orderService.cancelOrder(new OrderId(orderId))));

        } catch (InvalidOrderStateTransitionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        } catch (OrderBusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        try {
            return ResponseEntity.ok(new OrderResponse(orderService.findOrder(new OrderId(orderId))));

        } catch (OrderBusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable UUID customerId) {
        try {
            List<Order> orders = orderService.findOrdersByCustomer(new CustomerId(customerId));

            // should be moved to a mapper
            List<OrderResponse> responses = orders.stream()
                    .map(OrderResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (OrderBusinessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }
}