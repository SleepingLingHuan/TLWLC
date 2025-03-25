package com.campus.trade.campustradebackend.service;

import com.campus.trade.campustradebackend.dto.LoginRequest;
import com.campus.trade.campustradebackend.dto.LoginResponse;
import com.campus.trade.campustradebackend.dto.RegisterRequest;
import com.campus.trade.campustradebackend.entity.User;
import com.campus.trade.campustradebackend.repository.UserRepository;
import com.campus.trade.campustradebackend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtService jwtService;

    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("手机号已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已存在");
        }

        // 检查学号是否已存在
        if (userRepository.findByStudentId(request.getStudentId()).isPresent()) {
            throw new RuntimeException("学号已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 直接使用原始密码
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStudentId(request.getStudentId());

        // 保存用户
        userRepository.save(user);

        // 生成token
        String token = jwtService.generateToken(user.getUsername());

        // 创建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setMessage("注册成功");

        return response;
    }

    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByPhone(request.getUsername())
                        .orElseGet(() -> userRepository.findByStudentId(request.getUsername())
                                .orElse(null)));

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成token
        String token = jwtService.generateToken(user.getUsername());

        // 创建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setMessage("登录成功");

        // 设置用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        response.setUserInfo(userInfo);

        return response;
    }
} 