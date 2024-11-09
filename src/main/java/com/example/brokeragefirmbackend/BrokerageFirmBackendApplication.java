package com.example.brokeragefirmbackend;

import com.example.brokeragefirmbackend.config.ApplicationProperties;
import com.example.brokeragefirmbackend.config.CustomPermissionEvaluator;
import com.example.brokeragefirmbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableConfigurationProperties(ApplicationProperties.class)
@SpringBootApplication
public class BrokerageFirmBackendApplication {

    @Autowired
    AuthService authService;

    public static void main(String[] args) {
        SpringApplication.run(BrokerageFirmBackendApplication.class, args);
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new CustomPermissionEvaluator(authService));
        return expressionHandler;
    }
}
