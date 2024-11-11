package com.example.brokeragefirmbackend.controller;

import com.example.brokeragefirmbackend.service.AuthService;
import com.example.brokeragefirmbackend.util.Constants;
import com.example.brokeragefirmbackend.util.PathConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(PathConstants.API_AUTH)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(PathConstants.LOGIN)
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get(Constants.USERNAME);
        String password = request.get(Constants.PASSWORD);

        String token = authService.authenticate(username, password);
        return ResponseEntity.ok(Map.of(Constants.TOKEN, token));
    }
}