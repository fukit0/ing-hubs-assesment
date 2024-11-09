package com.example.brokeragefirmbackend.controller;

import com.example.brokeragefirmbackend.model.Asset;
import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.repository.CustomerRepository;
import com.example.brokeragefirmbackend.service.AssetService;
import com.example.brokeragefirmbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(principal, #customerId)")
    @PostMapping("/deposit")
    public ResponseEntity<String> depositMoney(@RequestParam Long customerId, @RequestParam BigDecimal amount) {
        assetService.depositMoney(customerId, amount);
        return ResponseEntity.ok("Money deposited successfully.");
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(principal, #customerId)")
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawMoney(@RequestParam Long customerId, @RequestParam BigDecimal amount, @RequestParam String iban) {
        assetService.withdrawMoney(customerId, amount, iban);
        return ResponseEntity.ok("Money withdrawn successfully.");
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(principal, #customerId)")
    @GetMapping("/list")
    public ResponseEntity<List<Asset>> listAssets(@RequestParam Long customerId) {
        List<Asset> assets = assetService.listAssets(customerId);
        return ResponseEntity.ok(assets);
    }
}