package com.example.brokeragefirmbackend.repository;

import com.example.brokeragefirmbackend.model.Order;
import com.example.brokeragefirmbackend.model.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDate startDate, LocalDate endDate);

    Optional<Order> findById(Long orderId);

    @Query("SELECT o FROM Order o WHERE o.status = com.example.brokeragefirmbackend.enums.OrderStatus.PENDING")
    List<Order> findPendingOrders();
}