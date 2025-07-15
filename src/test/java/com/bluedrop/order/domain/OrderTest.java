package com.bluedrop.order.domain;

import com.bluedrop.order.application.request.PlaceOrderRequest;
import com.bluedrop.order.application.response.ItemResponse;
import com.bluedrop.order.application.response.OrderResponse;
import com.bluedrop.order.domain.event.DomainEvent;
import com.bluedrop.order.domain.event.OrderApprovedEvent;
import com.bluedrop.order.domain.event.OrderCancelledEvent;
import com.bluedrop.order.domain.event.OrderPlacedEvent;
import com.bluedrop.order.domain.exceptions.InvalidOrderStateTransitionException;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import com.bluedrop.order.domain.repository.InMemoryOrderRepository;
import com.bluedrop.order.domain.repository.OrderRepository;
import com.bluedrop.order.domain.service.OrderService;
import com.bluedrop.order.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bluedrop.order.domain.util.OrderTestUtil.*;


// Order Aggregate Test Class
public class OrderTest {

    @Test
    public void testOrderCreation() {
        // Test successful order creation
        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Order order = getOrder(orderId, customerId);

        // Test order total calculation
        Price expectedTotal = new Price(new BigDecimal("1059.97"), "USD");
        assert order.getTotalAmount().equals(expectedTotal) : "Order total should be calculated correctly";

        // Test validation - empty items
        try {
            new Order(orderId, customerId, new ArrayList<>());
            assert false : "Should throw exception for empty items";
        } catch (OrderBusinessException e) {
            // Expected
        }
    }

    @Test
    public void testOrderApproval() {
        Order order = createTestOrder();
        assert order.getStatus() == OrderStatus.PENDING : "Order should start as PENDING";

        order.approve();
        assert order.getStatus() == OrderStatus.APPROVED : "Order should be APPROVED after approval";

        // Test that domain event was raised
        boolean hasApprovalEvent = order.getDomainEvents().stream()
                .anyMatch(event -> event instanceof OrderApprovedEvent);
        assert hasApprovalEvent : "Should have OrderApprovedEvent";
    }

    @Test
    public void testOrderCancellation() {
        Order order = createTestOrder();
        assert order.getStatus() == OrderStatus.PENDING : "Order should start as PENDING";

        order.cancel();
        assert order.getStatus() == OrderStatus.CANCELLED : "Order should be CANCELLED after cancellation";

        // Test that domain event was raised
        boolean hasCancelEvent = order.getDomainEvents().stream()
                .anyMatch(event -> event instanceof OrderCancelledEvent);
        assert hasCancelEvent : "Should have OrderCancelledEvent";
    }

    @Test
    public void testInvalidStateTransitions() {
        // Test approving already approved order
        Order approvedOrder = createTestOrder();
        approvedOrder.approve();

        try {
            approvedOrder.approve();
            assert false : "Should not allow approving already approved order";
        } catch (InvalidOrderStateTransitionException e) {
            // Expected
        }

        // Test cancelling already approved order
        try {
            approvedOrder.cancel();
            assert false : "Should not allow cancelling approved order";
        } catch (InvalidOrderStateTransitionException e) {
            // Expected
        }

        // Test approving canceled order
        Order cancelledOrder = createTestOrder();
        cancelledOrder.cancel();

        try {
            cancelledOrder.approve();
            assert false : "Should not allow approving cancelled order";
        } catch (InvalidOrderStateTransitionException e) {
            // Expected
        }

        // Test cancelling already canceled order
        try {
            cancelledOrder.cancel();
            assert false : "Should not allow cancelling already cancelled order";
        } catch (InvalidOrderStateTransitionException e) {
            // Expected
        }
    }

    @Test
    public void testOrderService() {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderService orderService = new OrderService(repository);

        OrderId orderId = new OrderId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        List<Item> items = List.of(
                new Item(new ProductId(UUID.randomUUID()), "Product 1",
                        new Price(new BigDecimal("50.00"), "USD"))
        );

        // Test place order
        Order placedOrder = orderService.placeOrder(orderId, customerId, items);
        assert placedOrder.getStatus() == OrderStatus.PENDING : "Placed order should be PENDING";

        // Test approve order
        orderService.approveOrder(orderId);
        Order retrievedOrder = repository.findById(orderId.id()).orElse(null);
        assert retrievedOrder != null : "Order should exist in repository";
        assert retrievedOrder.getStatus() == OrderStatus.APPROVED : "Order should be APPROVED";

        // Test duplicate order placement
        try {
            orderService.placeOrder(orderId, customerId, items);
            assert false : "Should not allow duplicate order IDs";
        } catch (OrderBusinessException e) {
            // Expected
        }

        // Test cancel non-existent order
        try {
            orderService.cancelOrder(new OrderId(UUID.randomUUID()));
            assert false : "Should throw exception for non-existent order";
        } catch (OrderBusinessException e) {
            // Expected
        }
    }

    @Test
    public void testValueObjects() {
        // Test OrderId equality
        OrderId id1 = new OrderId(UUID.randomUUID());
        OrderId id2 = new OrderId(id1.id());
        OrderId id3 = new OrderId(UUID.randomUUID());

        assert id1.equals(id2) : "Same OrderIds should be equal";
        assert !id1.equals(id3) : "Different OrderIds should not be equal";
        assert id1.hashCode() == id2.hashCode() : "Equal OrderIds should have same hash code";

        // Test Price operations
        Price price1 = new Price(new BigDecimal("10.00"), "USD");
        Price price2 = new Price(new BigDecimal("5.00"), "USD");
        Price price3 = price1.add(price2);

        assert price3.amount().equals(new BigDecimal("15.00")) : "Price addition should work correctly";

        Price multiplied = price1.multiply(3);
        assert multiplied.amount().equals(new BigDecimal("30.00")) : "Price multiplication should work correctly";

        // Test currency validation
        try {
            price1.add(new Price(new BigDecimal("5.00"), "EUR"));
            assert false : "Should not allow adding different currencies";
        } catch (OrderBusinessException e) {
            // Expected
        }
    }

    @Test
    public void testDomainEvents() {
        Order order = createTestOrder();

        // Check OrderPlacedEvent
        List<DomainEvent> events = order.getDomainEvents();
        assert events.size() == 1 : "Should have one domain event initially";
        assert events.getFirst() instanceof OrderPlacedEvent : "Should have OrderPlacedEvent";

        OrderPlacedEvent placedEvent = (OrderPlacedEvent) events.getFirst();
        assert placedEvent.getOrderId().equals(order.getOrderId()) : "Event should have correct order ID";
        assert placedEvent.getCustomerId().equals(order.getCustomerId()) : "Event should have correct customer ID";

        // Check OrderApprovedEvent
        order.approve();
        events = order.getDomainEvents();
        assert events.size() == 2 : "Should have two domain events after approval";
        assert events.get(1) instanceof OrderApprovedEvent : "Should have OrderApprovedEvent";

        // Test clearing events
        order.clearDomainEvents();
        assert order.getDomainEvents().isEmpty() : "Domain events should be cleared";
    }

    @Test
    public void testRestApiDtos() {
        // Test PlaceOrderRequest
        PlaceOrderRequest placeRequest = getPlaceOrderRequest();
        assert placeRequest.getItems().size() == 1 : "PlaceOrderRequest should have correct items";

        // Test OrderResponse conversion
        Order order = createTestOrder();
        OrderResponse orderResponse = new OrderResponse(order);

        assert UUID.fromString(orderResponse.getOrderId()).equals(order.getOrderId().id()) : "OrderResponse should have correct order ID";
        assert UUID.fromString(orderResponse.getCustomerId()).equals(order.getCustomerId().id()) : "OrderResponse should have correct customer ID";
        assert orderResponse.getStatus().equals(order.getStatus().name()) : "OrderResponse should have correct status";
        assert orderResponse.getItems().size() == order.getItems().size() : "OrderResponse should have correct number of items";

        // Test ItemResponse conversion
        Item item = order.getItems().getFirst();
        ItemResponse itemResponse = new ItemResponse(item);

        assert UUID.fromString(itemResponse.getProductId()).equals(item.productId().id()) : "ItemResponse should have correct product ID";
        assert itemResponse.getProductName().equals(item.productName()) : "ItemResponse should have correct product name";
        assert itemResponse.getPrice().equals(item.price().amount()) : "ItemResponse should have correct price";
        assert itemResponse.getCurrency().equals(item.price().currency()) : "ItemResponse should have correct currency";
    }
}