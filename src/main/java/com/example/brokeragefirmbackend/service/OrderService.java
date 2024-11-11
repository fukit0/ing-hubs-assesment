package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.model.Order;
import com.example.brokeragefirmbackend.model.Asset;
import com.example.brokeragefirmbackend.enums.OrderStatus;
import com.example.brokeragefirmbackend.enums.OrderSide;
import com.example.brokeragefirmbackend.repository.AssetRepository;
import com.example.brokeragefirmbackend.repository.OrderRepository;
import com.example.brokeragefirmbackend.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.brokeragefirmbackend.util.Constants.TRY;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }

    /**
     * Create a new order for a customer, checking if they have enough assets or TRY balance.
     */
    @Transactional
    public Order createOrder(Long customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        // Check if the customer has enough assets or TRY balance
        Optional<Asset> assetOptional = assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
        if (assetOptional.isEmpty()) {
            throw new IllegalArgumentException(Constants.ASSET_NOT_FOUND);
        }
        Asset asset = assetOptional.get();

        // Check the side of the order (BUY or SELL) and validate accordingly
        if (side == OrderSide.BUY) {
            // Check if customer has enough TRY to place a BUY order
            Optional<Asset> tryAssetOptional = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY");
            BigDecimal totalPrice = price.multiply(size);
            if (tryAssetOptional.isEmpty() || tryAssetOptional.get().getUsableSize().compareTo(totalPrice) < 0) {
                throw new IllegalArgumentException(Constants.INSUFFICIENT_BALANCE);
            }
            // Deduct TRY balance after successful check
            Asset tryAsset = tryAssetOptional.get();
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalPrice));
            assetRepository.save(tryAsset);
        } else if (side == OrderSide.SELL) {
            // Check if customer has enough of the asset to sell
            if (asset.getUsableSize().compareTo(size) < 0) {
                throw new IllegalArgumentException(Constants.INSUFFICIENT_BALANCE);
            }
            // Deduct asset size after successful check
            asset.setUsableSize(asset.getUsableSize().subtract(size));
            assetRepository.save(asset);
        }

        // Create and save the order
        Order order = new Order(null, customerId, assetName, side, size, price, OrderStatus.PENDING, LocalDate.now());
        return orderRepository.save(order);
    }

    /**
     * List orders for a specific customer within a date range.
     */
    public List<Order> listOrders(Long customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(Constants.INVALID_DATE_RANGE);
        }
        if (startDate.isAfter(LocalDate.now()) || endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(Constants.INVALID_DATE_RANGE);
        }
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }


    /**
     * Cancel a pending order. Updates asset or TRY balance based on the order's side.
     */
    @Transactional
    public void cancelOrder(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(Constants.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(Constants.ORDER_STATUS_SHOULD_BE_PENDING);
        }

        if(!Objects.equals(order.getCustomerId(), customerId)) {
            throw new IllegalArgumentException(Constants.ASSET_NOT_FOUND);
        }

        // Update asset or TRY balance based on order side
        if (order.getSide() == OrderSide.BUY) {
            // Refund TRY balance
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY)
                    .orElseThrow(() -> new IllegalArgumentException(Constants.ASSET_NOT_FOUND));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(order.getPrice().multiply(order.getSize())));
            assetRepository.save(tryAsset);
        } else if (order.getSide() == OrderSide.SELL) {
            // Refund asset balance
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new IllegalArgumentException(Constants.ASSET_NOT_FOUND));
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        // Update order status to canceled
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void matchPendingOrders() {
        // Fetch pending orders
        List<Order> pendingOrders = orderRepository.findPendingOrders();

        // Iterate through each order and update assets accordingly
        for (Order order : pendingOrders) {
            updateAssets(order);
        }
    }

    private void updateAssets(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            // Update the bought asset size
            Asset boughtAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new IllegalArgumentException(Constants.ASSET_NOT_FOUND));
            boughtAsset.setSize(boughtAsset.getSize().add(order.getSize()));
            boughtAsset.setUsableSize(boughtAsset.getUsableSize().add(order.getSize()));
            assetRepository.save(boughtAsset);
        } else if (order.getSide() == OrderSide.SELL) {
            // Update the TRY asset size
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY)
                    .orElseThrow(() -> new IllegalArgumentException(Constants.ASSET_NOT_FOUND));
            BigDecimal totalPrice = order.getPrice().multiply(order.getSize());
            tryAsset.setSize(tryAsset.getSize().add(totalPrice));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalPrice));
            assetRepository.save(tryAsset);
        }
    }
}