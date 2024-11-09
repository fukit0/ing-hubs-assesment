package com.example.brokeragefirmbackend.controller;

import com.example.brokeragefirmbackend.model.Order;
import com.example.brokeragefirmbackend.model.OrderRequest;
import com.example.brokeragefirmbackend.service.AuthService;
import com.example.brokeragefirmbackend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(principal, #customerId)")
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestParam Long customerId, @RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(customerId, orderRequest.getAssetName(), orderRequest.getSide(), orderRequest.getSize(), orderRequest.getPrice());
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(principal, #customerId)")
    @GetMapping("/list")
    public ResponseEntity<List<Order>> listOrders(@RequestParam Long customerId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Order> orders = orderService.listOrders(customerId, startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(principal, #customerId)")
    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<String> deleteOrder(@RequestParam Long customerId, @PathVariable Long orderId) {
        orderService.cancelOrder(customerId, orderId);
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/match")
    public ResponseEntity<String> matchPendingOrders() {
        orderService.matchPendingOrders();
        return ResponseEntity.ok("Orders matched and assets updated successfully.");
    }
}