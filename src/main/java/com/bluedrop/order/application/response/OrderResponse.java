package com.bluedrop.order.application.response;

import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.valueobjects.Price;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class OrderResponse {
    private String orderId;
    private String customerId;
    private String status;
    private LocalDateTime createdAt;
    private List<ItemResponse> items;
    private BigDecimal totalAmount;
    private String currency;

    public OrderResponse(Order order) {
        this.orderId = String.valueOf(order.getOrderId().id());
        this.customerId = String.valueOf(order.getCustomerId().id());
        this.status = order.getStatus().name();
        this.createdAt = order.getCreatedAt();
        this.items = order.getItems().stream()
                .map(ItemResponse::new)
                .collect(Collectors.toList());
        Price total = order.getTotalAmount();
        this.totalAmount = total.amount();
        this.currency = total.currency();
    }

}
