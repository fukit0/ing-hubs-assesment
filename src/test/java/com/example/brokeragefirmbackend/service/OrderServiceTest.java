package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.enums.OrderSide;
import com.example.brokeragefirmbackend.enums.OrderStatus;
import com.example.brokeragefirmbackend.model.Asset;
import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.model.Order;
import com.example.brokeragefirmbackend.model.OrderRequest;
import com.example.brokeragefirmbackend.repository.OrderRepository;
import com.example.brokeragefirmbackend.repository.AssetRepository;
import com.example.brokeragefirmbackend.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOrder() {
        OrderRequest orderRequest = new OrderRequest();
        // Set up orderRequest properties

        Order order = new Order();
        // Set up order properties

        when(assetRepository.findByCustomerIdAndAssetName(orderRequest.getCustomerId(), orderRequest.getAssetName()))
                .thenReturn(Optional.of(new Asset())); // Mock the asset retrieval

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order createdOrder = orderService.createOrder(orderRequest.getCustomerId(), orderRequest.getAssetName(), orderRequest.getSide(), orderRequest.getSize(), orderRequest.getPrice());

        assertNotNull(createdOrder);
        // Add more assertions as needed
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(assetRepository, times(1)).findByCustomerIdAndAssetName(orderRequest.getCustomerId(), orderRequest.getAssetName());
    }

    @Test
    public void testListOrders() {
        Long customerId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        List<Order> orders = Arrays.asList(new Order(), new Order());
        when(orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate)).thenReturn(orders);

        List<Order> result = orderService.listOrders(customerId, startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Test
    public void testCancelOrder() {
        Long orderId = 1L;
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING); // Set the order status to pending

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.cancelOrder(order.getCustomerId(), orderId);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        assertEquals(OrderStatus.CANCELLED, order.getStatus()); // Verify the order status is updated to canceled
    }

    @Test
    public void testCreateOrder_InsufficientTRYBalance() {
        Customer customer = new Customer(1L);
        String assetName = "ASSET";
        OrderSide side = OrderSide.BUY;
        BigDecimal size = BigDecimal.valueOf(10);
        BigDecimal price = BigDecimal.valueOf(100);

        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), assetName))
                .thenReturn(Optional.of(new Asset()));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(Optional.of(new Asset(customer, "TRY", BigDecimal.valueOf(500))));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(customer.getId(), assetName, side, size, price);
        });

        assertEquals(Constants.INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    public void testCreateOrder_InsufficientAssetBalance() {
        Long customerId = 1L;
        String assetName = "ASSET";
        OrderSide side = OrderSide.SELL;
        BigDecimal size = BigDecimal.valueOf(10);
        BigDecimal price = BigDecimal.valueOf(100);

        Asset asset = new Asset();
        asset.setUsableSize(BigDecimal.valueOf(5));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName))
                .thenReturn(Optional.of(asset));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(customerId, assetName, side, size, price);
        });

        assertEquals(Constants.INSUFFICIENT_BALANCE, exception.getMessage());
    }

    @Test
    public void testListOrders_InvalidDateRange() {
        Long customerId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.listOrders(customerId, startDate, endDate);
        });

        assertEquals("Start date must be before end date.", exception.getMessage());
    }

    @Test
    public void testCancelOrder_NonPendingOrder() {
        Long orderId = 1L;
        Order order = new Order();
        order.setStatus(OrderStatus.FILLED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(order.getCustomerId(), orderId);
        });

        assertEquals("Only pending orders can be canceled.", exception.getMessage());
    }

    @Test
    public void testCancelOrder_OrderDoesNotBelongToCustomer() {
        Long customerId = 1L;
        Long orderId = 1L;
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerId(2L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.cancelOrder(customerId, orderId);
        });

        assertEquals("Order does not belong to the customer.", exception.getMessage());
    }
}