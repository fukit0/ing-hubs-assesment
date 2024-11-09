package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.repository.CustomerRepository;
import com.example.brokeragefirmbackend.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;

    private final JwtUtil jwtUtil;

    public AuthService(CustomerRepository customerRepository, JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }


    public String authenticate(String username, String password) {
        Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!Objects.equals(password, customer.getPassword())) { // Changed from passwordEncoder.matches(password, customer.getPassword())
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(username, customer.isAdmin());
    }

    public boolean isCustomer(String email, Long customerId) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return customer.getId().equals(customerId);
    }

    public Customer getCurrentCustomer() {
        return (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}