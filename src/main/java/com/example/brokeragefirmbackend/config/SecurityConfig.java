package com.example.brokeragefirmbackend.config;

import com.example.brokeragefirmbackend.model.Customer;
import com.example.brokeragefirmbackend.repository.CustomerRepository;
import com.example.brokeragefirmbackend.util.Constants;
import com.example.brokeragefirmbackend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerRepository customerRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/assets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, customerRepository), BasicAuthenticationFilter.class);

        return http.build();
    }

    private static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;

        private final CustomerRepository customerRepository;

        public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomerRepository customerRepository) {
            this.jwtUtil = jwtUtil;
            this.customerRepository = customerRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String authHeader = request.getHeader(Constants.AUTH_HEADER);
            if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
                String token = authHeader.substring(7);
                Claims claims = jwtUtil.extractClaims(token);
                String email = claims.getSubject();
                Boolean isAdmin = claims.get(Constants.IS_ADMIN, Boolean.class);

                Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(Constants.CUSTOMER_NOT_FOUND));
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customer, null, isAdmin ? List.of(new SimpleGrantedAuthority(Constants.ROLE_ADMIN)) : List.of());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        }
    }

}