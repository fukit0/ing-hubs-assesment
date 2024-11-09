package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.model.Asset;
import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDepositMoney() {
        Customer c = new Customer();
        c.setId(1L);

        BigDecimal amount = new BigDecimal("100.00");
        Asset asset = new Asset(c, "TRY", new BigDecimal("50.00"));

        when(assetRepository.findByCustomerIdAndAssetName(c.getId(), "TRY")).thenReturn(Optional.of(asset));
        when(authService.getCurrentCustomer()).thenReturn(c);

        assetService.depositMoney(c.getId(), amount);

        assertEquals(new BigDecimal("150.00"), asset.getSize());
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    void testWithdrawMoney() {
        Customer c = new Customer();
        c.setId(1L);

        BigDecimal amount = new BigDecimal("50.00");
        String iban = "TR1234567890";
        Asset asset = new Asset(c, "TRY", new BigDecimal("100.00"));
        asset.setUsableSize(new BigDecimal("100.00"));

        when(assetRepository.findByCustomerIdAndAssetName(c.getId(), "TRY")).thenReturn(Optional.of(asset));

        assetService.withdrawMoney(c.getId(), amount, iban);

        assertEquals(new BigDecimal("50.00"), asset.getSize());
        assertEquals(new BigDecimal("50.00"), asset.getUsableSize());
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    void testListAssets() {
        Customer c = new Customer();
        c.setId(1L);

        List<Asset> assets = Arrays.asList(
                new Asset(c, "TRY", new BigDecimal("100.00")),
                new Asset(c, "USD", new BigDecimal("200.00"))
        );

        when(assetRepository.findByCustomerId(c.getId())).thenReturn(assets);

        List<Asset> result = assetService.listAssets(c.getId());

        assertEquals(2, result.size());
        assertEquals("TRY", result.get(0).getAssetName());
        assertEquals("USD", result.get(1).getAssetName());
    }
}