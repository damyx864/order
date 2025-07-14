package com.bluedrop.order.application;

import com.bluedrop.order.application.controller.OrderController;
import com.bluedrop.order.application.request.ItemRequest;
import com.bluedrop.order.application.request.PlaceOrderRequest;
import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.OrderStatus;
import com.bluedrop.order.domain.exceptions.InvalidOrderStateTransitionException;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import com.bluedrop.order.domain.service.OrderService;
import com.bluedrop.order.domain.valueobjects.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testOrderId;
    private UUID testCustomerId;
    private UUID testProductId;
    private Order testOrder;
    private PlaceOrderRequest testPlaceOrderRequest;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        // Create test order
        List<Item> items = List.of(
                new Item(new ProductId(testProductId), "Test Product",
                        new Price(new BigDecimal("100.00"), "USD"))
        );
        testOrder = new Order(new OrderId(testOrderId), new CustomerId(testCustomerId), items);

        // Create items test request
        ItemRequest itemRequest = new ItemRequest(testProductId.toString(), "Test Product",
                new BigDecimal("100.00"), "USD");
        testPlaceOrderRequest = new PlaceOrderRequest(testOrderId.toString(), testCustomerId.toString(), List.of(itemRequest));
    }

    @Test
    void testPlaceOrder_Success() throws Exception {
        Mockito.when(orderService.placeOrder(any(OrderId.class),
                        any(CustomerId.class),
                        anyList()))
                .thenReturn(testOrder);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPlaceOrderRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(testOrderId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(testCustomerId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].productId").value(testProductId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].productName").value("Test Product"));
    }

    @Test
    void testPlaceOrder_OrderBusinessException() throws Exception {
        Mockito.when(orderService.placeOrder(any(OrderId.class), any(CustomerId.class), anyList()))
                .thenThrow(new OrderBusinessException("Order already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPlaceOrderRequest)))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testPlaceOrder_NegativePrice() throws Exception {
        testPlaceOrderRequest.getItems().getFirst().setPrice(new BigDecimal("-100.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPlaceOrderRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        verify(orderService, never()).placeOrder(any(OrderId.class), any(CustomerId.class), anyList());
    }

    @Test
    void testPlaceOrder_IllegalArgumentException() throws Exception {
        Mockito.when(orderService.placeOrder(any(OrderId.class), any(CustomerId.class), anyList()))
                .thenThrow(new IllegalArgumentException("Invalid order data"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPlaceOrderRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testPlaceOrder_InvalidJson() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testPlaceOrder_EmptyItems() throws Exception {
        PlaceOrderRequest emptyItemsRequest = new PlaceOrderRequest(
                testOrderId.toString(),
                testCustomerId.toString(),
                Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyItemsRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testApproveOrder_Success() throws Exception {
        Order approvedOrder = new Order(new OrderId(testOrderId), new CustomerId(testCustomerId),
                List.of(new Item(new ProductId(testProductId), "Test Product",
                        new Price(new BigDecimal("100.00"), "USD"))));
        approvedOrder.approve();

        Mockito.when(orderService.approveOrder(approvedOrder.getOrderId()))
                .thenReturn(approvedOrder);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/approve", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(testOrderId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testApproveOrder_OrderNotFound() throws Exception {
        Mockito.doThrow(new OrderBusinessException("Order not found"))
                .when(orderService).approveOrder(any(OrderId.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/approve", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testApproveOrder_InvalidStateTransition_fromCanceledToApproved() throws Exception {
        Mockito.doThrow(new InvalidOrderStateTransitionException(OrderStatus.CANCELLED, OrderStatus.APPROVED))
                .when(orderService).approveOrder(any(OrderId.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/approve", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testApproveOrder_InvalidUuid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/approve", "invalid-uuid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testCancelOrder_Success() throws Exception {
        Order cancelledOrder = new Order(new OrderId(testOrderId), new CustomerId(testCustomerId),
                List.of(new Item(new ProductId(testProductId), "Test Product",
                        new Price(new BigDecimal("100.00"), "USD"))));
        cancelledOrder.cancel();

        Mockito.when(orderService.cancelOrder(cancelledOrder.getOrderId()))
                .thenReturn(cancelledOrder);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/cancel", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(testOrderId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testCancelOrder_OrderNotFound() throws Exception {
        Mockito.doThrow(new OrderBusinessException("Order not found"))
                .when(orderService).cancelOrder(any(OrderId.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/cancel", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCancelOrder_InvalidStateTransition_fromApprovedToCanceled() throws Exception {
        Mockito.when(orderService.cancelOrder(any(OrderId.class)))
                .thenThrow(new InvalidOrderStateTransitionException(OrderStatus.APPROVED, OrderStatus.CANCELLED));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/cancel", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void testCancelOrder_InvalidUuid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/cancel", "invalid-uuid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testGetOrder_Success() throws Exception {
        Mockito.when(orderService.findOrder(any(OrderId.class)))
                .thenReturn(testOrder);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{orderId}", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(testOrderId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value(testCustomerId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1));
    }

    @Test
    void testGetOrder_NotFound() throws Exception {
        Mockito.doThrow(new OrderBusinessException("Order not found"))
                .when(orderService).findOrder(any(OrderId.class));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{orderId}", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testGetOrder_InvalidUuid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{orderId}", "invalid-uuid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testGetOrdersByCustomer_Success() throws Exception {
        List<Order> orders = Collections.singletonList(testOrder);
        Mockito.when(orderService.findOrdersByCustomer(any(CustomerId.class)))
                .thenReturn(orders);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/customer/{customerId}", testCustomerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderId").value(testOrderId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].customerId").value(testCustomerId.toString()));
    }

    @Test
    void testGetOrdersByCustomer_EmptyList() throws Exception {
        Mockito.when(orderService.findOrdersByCustomer(any(CustomerId.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/customer/{customerId}", testCustomerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }

    @Test
    void testGetOrdersByCustomer_InvalidUuid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/customer/{customerId}", "invalid-uuid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testEndToEndFlow() throws Exception {
        // Step 1: Place order
        Mockito.when(orderService.placeOrder(any(OrderId.class), any(CustomerId.class), anyList()))
                .thenReturn(testOrder);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPlaceOrderRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"));

        // Step 2: Approve order
        Order approvedOrder = new Order(new OrderId(testOrderId), new CustomerId(testCustomerId),
                List.of(new Item(new ProductId(testProductId), "Test Product",
                        new Price(new BigDecimal("100.00"), "USD"))));
        approvedOrder.approve();

        Mockito.when(orderService.approveOrder(any(OrderId.class)))
                .thenReturn(approvedOrder);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/{orderId}/approve", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("APPROVED"));

        // Step 3: Get order by ID
        Mockito.when(orderService.findOrder(any(OrderId.class)))
                .thenReturn(approvedOrder);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{orderId}", testOrderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("APPROVED"));

        // Step 4: Get orders by customer
        Mockito.when(orderService.findOrdersByCustomer(any(CustomerId.class)))
                .thenReturn(List.of(approvedOrder));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/customer/{customerId}", testCustomerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void testMultipleItemsOrder() throws Exception {
        List<ItemRequest> multipleItems = Arrays.asList(
                new ItemRequest(UUID.randomUUID().toString(), "Product 1",
                        new BigDecimal("50.00"), "USD"),
                new ItemRequest(UUID.randomUUID().toString(), "Product 2",
                        new BigDecimal("75.00"), "USD")
        );

        PlaceOrderRequest multipleItemsRequest = new PlaceOrderRequest(
                testOrderId.toString(),
                testCustomerId.toString(),
                multipleItems);

        List<Item> orderItems = Arrays.asList(
                new Item(new ProductId(UUID.randomUUID()), "Product 1",
                        new Price(new BigDecimal("50.00"), "USD")),
                new Item(new ProductId(UUID.randomUUID()), "Product 2",
                        new Price(new BigDecimal("75.00"), "USD"))
        );

        Order multipleItemsOrder = new Order(new OrderId(testOrderId),
                new CustomerId(testCustomerId), orderItems);

        Mockito.when(orderService.placeOrder(any(OrderId.class), any(CustomerId.class), anyList()))
                .thenReturn(multipleItemsOrder);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(multipleItemsRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].productName").value("Product 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].productName").value("Product 2"));
    }
}