package com.example.brokeragefirmbackend.model;

import com.example.brokeragefirmbackend.enums.OrderSide;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class OrderRequest {
    private Long customerId;
    private String assetName;
    private OrderSide side;
    private BigDecimal size;
    private BigDecimal price;

}