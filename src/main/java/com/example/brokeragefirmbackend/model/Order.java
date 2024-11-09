package com.example.brokeragefirmbackend.model;

import com.example.brokeragefirmbackend.enums.OrderSide;
import com.example.brokeragefirmbackend.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"order\"")
public class Order {

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String assetName;
    private OrderSide side;
    private BigDecimal size;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDate createDate;

}
