package com.example.brokeragefirmbackend.config;

import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.service.AuthService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AuthService authService;

    public CustomPermissionEvaluator(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        Customer customer = (Customer) targetDomainObject;
        return authService.isCustomer(customer.getEmail(), (Long) permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
