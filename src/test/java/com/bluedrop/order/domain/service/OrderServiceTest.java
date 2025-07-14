package com.bluedrop.order.domain.service;

import com.bluedrop.order.domain.Order;
import com.bluedrop.order.domain.OrderStatus;
import com.bluedrop.order.domain.exceptions.InvalidOrderStateTransitionException;
import com.bluedrop.order.domain.exceptions.OrderBusinessException;
import com.bluedrop.order.domain.repository.OrderRepository;
import com.bluedrop.order.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderId testOrderId;
    private CustomerId testCustomerId;
    private List<Item> testItems;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrderId = new OrderId(UUID.randomUUID());
        testCustomerId = new CustomerId(UUID.randomUUID());
        testItems = List.of(
                new Item(new ProductId(UUID.randomUUID()), "Test Product",
                        new Price(new BigDecimal("100.00"), "USD"))
        );
        testOrder = new Order(testOrderId, testCustomerId, testItems);
    }

    @Test
    void testPlaceOrder_Success() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.placeOrder(testOrderId, testCustomerId, testItems);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderId, result.getOrderId());
        assertEquals(testCustomerId, result.getCustomerId());
        assertEquals(testItems.size(), result.getItems().size());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_OrderAlreadyExists() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(testOrder));

        // Act & Assert
        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () ->
                orderService.placeOrder(testOrderId, testCustomerId, testItems));

        assertEquals("Order with ID " + testOrderId + " already exists", exception.getMessage());
        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_EmptyItems() {
        // Arrange
        List<Item> emptyItems = Collections.emptyList();
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderBusinessException.class, () ->
                orderService.placeOrder(testOrderId, testCustomerId, emptyItems));

        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_NullItems() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderBusinessException.class, () ->
                orderService.placeOrder(testOrderId, testCustomerId, null));

        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testApproveOrder_Success() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(testOrder));
        Order approvedOrder = new Order(testOrderId, testCustomerId, testItems);
        approvedOrder.approve();
        when(orderRepository.save(any(Order.class))).thenReturn(approvedOrder);

        // Act
        Order result = orderService.approveOrder(testOrderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.APPROVED, result.getStatus());
        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testApproveOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());

        // Act & Assert
        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () ->
                orderService.approveOrder(testOrderId));

        assertEquals("Order not found: " + testOrderId, exception.getMessage());
        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testApproveOrder_InvalidStateTransition() {
        // Arrange
        Order cancelledOrder = new Order(testOrderId, testCustomerId, testItems);
        cancelledOrder.cancel(); // Cancel first to make it invalid for approval
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(cancelledOrder));

        // Act & Assert
        assertThrows(InvalidOrderStateTransitionException.class, () ->
                orderService.approveOrder(testOrderId));

        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(testOrder));
        Order cancelledOrder = new Order(testOrderId, testCustomerId, testItems);
        cancelledOrder.cancel();
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        // Act
        Order result = orderService.cancelOrder(testOrderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());

        // Act & Assert
        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () ->
                orderService.cancelOrder(testOrderId));

        assertEquals("Order not found: " + testOrderId, exception.getMessage());
        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_InvalidStateTransition() {
        // Arrange
        Order approvedOrder = new Order(testOrderId, testCustomerId, testItems);
        approvedOrder.approve(); // Approve first to make it invalid for cancellation
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(approvedOrder));

        // Act & Assert
        assertThrows(InvalidOrderStateTransitionException.class, () ->
                orderService.cancelOrder(testOrderId));

        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testFindOrder_Success() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.findOrder(testOrderId);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderId, result.getOrderId());
        assertEquals(testCustomerId, result.getCustomerId());
        assertEquals(testItems.size(), result.getItems().size());
        verify(orderRepository).findById(testOrderId.id());
    }

    @Test
    void testFindOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());

        // Act & Assert
        OrderBusinessException exception = assertThrows(OrderBusinessException.class, () ->
                orderService.findOrder(testOrderId));

        assertEquals("Order not found: " + testOrderId, exception.getMessage());
        verify(orderRepository).findById(testOrderId.id());
    }

    @Test
    void testFindOrdersByCustomer_Success() {
        // Arrange
        List<Order> expectedOrders = List.of(testOrder);
        when(orderRepository.findByCustomerId(testCustomerId.id())).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.findOrdersByCustomer(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.getFirst());
        verify(orderRepository).findByCustomerId(testCustomerId.id());
    }

    @Test
    void testFindOrdersByCustomer_EmptyList() {
        // Arrange
        when(orderRepository.findByCustomerId(testCustomerId.id())).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.findOrdersByCustomer(testCustomerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByCustomerId(testCustomerId.id());
    }

    @Test
    void testFindOrdersByCustomer_MultipleOrders() {
        // Arrange
        Order secondOrder = new Order(new OrderId(UUID.randomUUID()), testCustomerId, testItems);
        List<Order> expectedOrders = List.of(testOrder, secondOrder);
        when(orderRepository.findByCustomerId(testCustomerId.id())).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.findOrdersByCustomer(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testOrder));
        assertTrue(result.contains(secondOrder));
        verify(orderRepository).findByCustomerId(testCustomerId.id());
    }

    @Test
    void testOrderLifecycle_PlaceApproveCancel() {
        // Arrange
        Order pendingOrder = new Order(testOrderId, testCustomerId, testItems);
        Order approvedOrder = new Order(testOrderId, testCustomerId, testItems);
        approvedOrder.approve();
        Order cancelledOrder = new Order(testOrderId, testCustomerId, testItems);
        cancelledOrder.cancel();

        // Place order
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        // Act - Place order
        Order placedOrder = orderService.placeOrder(testOrderId, testCustomerId, testItems);

        // Assert - Place order
        assertEquals(OrderStatus.PENDING, placedOrder.getStatus());

        // Approve order
        reset(orderRepository);
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(approvedOrder);

        // Act - Approve order
        Order approvedResult = orderService.approveOrder(testOrderId);

        // Assert - Approve order
        assertEquals(OrderStatus.APPROVED, approvedResult.getStatus());

        // Try to cancel the approved order (should fail)
        reset(orderRepository);
        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.of(approvedOrder));

        // Act & Assert - Cancel approved order
        assertThrows(InvalidOrderStateTransitionException.class, () ->
                orderService.cancelOrder(testOrderId));
    }

    @Test
    void testMultipleItemsOrder() {
        // Arrange
        List<Item> multipleItems = List.of(
                new Item(new ProductId(UUID.randomUUID()), "Product 1",
                        new Price(new BigDecimal("50.00"), "USD")),
                new Item(new ProductId(UUID.randomUUID()), "Product 2",
                        new Price(new BigDecimal("75.00"), "USD")),
                new Item(new ProductId(UUID.randomUUID()), "Product 3",
                        new Price(new BigDecimal("25.00"), "USD"))
        );
        Order multipleItemsOrder = new Order(testOrderId, testCustomerId, multipleItems);

        when(orderRepository.findById(testOrderId.id())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(multipleItemsOrder);

        // Act
        Order result = orderService.placeOrder(testOrderId, testCustomerId, multipleItems);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getItems().size());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).findById(testOrderId.id());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testRepositoryExceptionHandling() {
        // Arrange
        when(orderRepository.findById(testOrderId.id())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                orderService.findOrder(testOrderId));

        verify(orderRepository).findById(testOrderId.id());
    }
}