package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.enums.OrderStatus;
import com.example.brokeragefirmbackend.model.Asset;
import com.example.brokeragefirmbackend.model.Order;
import com.example.brokeragefirmbackend.model.OrderRequest;
import com.example.brokeragefirmbackend.repository.OrderRepository;
import com.example.brokeragefirmbackend.repository.AssetRepository;
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
}