package com.example.brokeragefirmbackend.service;

import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.repository.CustomerRepository;
import com.example.brokeragefirmbackend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateSuccess() {
        String username = "test@example.com";
        String password = "password";
        Customer customer = new Customer();
        customer.setEmail(username);
        customer.setPassword(password);
        customer.setAdmin(false);

        when(customerRepository.findByEmail(username)).thenReturn(Optional.of(customer));
        when(jwtUtil.generateToken(username, false)).thenReturn("token");

        String token = authService.authenticate(username, password);

        assertEquals("token", token);
        verify(customerRepository, times(1)).findByEmail(username);
        verify(jwtUtil, times(1)).generateToken(username, false);
    }

    @Test
    void testAuthenticateUserNotFound() {
        String username = "test@example.com";
        String password = "password";

        when(customerRepository.findByEmail(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.authenticate(username, password);
        });

        assertEquals("User not found", exception.getMessage());
        verify(customerRepository, times(1)).findByEmail(username);
    }

    @Test
    void testAuthenticateInvalidPassword() {
        String username = "test@example.com";
        String password = "password";
        Customer customer = new Customer();
        customer.setEmail(username);
        customer.setPassword("wrongpassword");
        customer.setAdmin(false);

        when(customerRepository.findByEmail(username)).thenReturn(Optional.of(customer));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(username, password);
        });

        assertEquals("Invalid password", exception.getMessage());
        verify(customerRepository, times(1)).findByEmail(username);
    }
}