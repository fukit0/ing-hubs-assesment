package com.example.brokeragefirmbackend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer; // ID of the customer who owns the asset

    private String assetName; // Name of the asset, e.g., "AAPL", "GOOG", or "TRY"

    private BigDecimal size; // Total amount of the asset owned by the customer

    private BigDecimal usableSize; // Portion of the asset available for transactions (e.g., not locked in pending orders)

    public Asset(Customer customer, String assetName, BigDecimal size) {
        this.customer = customer;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = size;
    }

}
