package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.model.Asset;
import com.example.brokeragefirmbackend.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AuthService authService;

    @Transactional
    public void depositMoney(Long customerId, BigDecimal amount) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
                .orElse(new Asset(authService.getCurrentCustomer(), "TRY", BigDecimal.ZERO));
        asset.setSize(asset.getSize().add(amount));
        asset.setUsableSize(asset.getUsableSize().add(amount));
        assetRepository.save(asset);
    }

    @Transactional
    public void withdrawMoney(Long customerId, BigDecimal amount, String iban) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
                .orElseThrow(() -> new IllegalArgumentException("Customer does not have a TRY asset"));

        if (asset.getUsableSize().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        asset.setSize(asset.getSize().subtract(amount));
        asset.setUsableSize(asset.getUsableSize().subtract(amount));
        assetRepository.save(asset);
    }

    public List<Asset> listAssets(Long customerId) {
        return assetRepository.findByCustomerId(customerId);
    }
}
