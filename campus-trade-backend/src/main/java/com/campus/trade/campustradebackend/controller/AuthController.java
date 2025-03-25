package com.campus.trade.campustradebackend.controller;

import com.campus.trade.campustradebackend.dto.LoginRequest;
import com.campus.trade.campustradebackend.dto.LoginResponse;
import com.campus.trade.campustradebackend.dto.RegisterRequest;
import com.campus.trade.campustradebackend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            logger.info("收到注册请求: {}", request);
            LoginResponse response = authService.register(request);
            logger.info("注册成功: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("注册失败", e);
            LoginResponse response = new LoginResponse();
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            logger.info("收到登录请求: {}", request);
            LoginResponse response = authService.login(request);
            logger.info("登录成功: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("登录失败", e);
            LoginResponse response = new LoginResponse();
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 