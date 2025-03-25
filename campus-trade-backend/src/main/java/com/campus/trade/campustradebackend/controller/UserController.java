package com.campus.trade.campustradebackend.controller;

import java.util.List;
import java.util.HashMap;
import com.campus.trade.campustradebackend.entity.User;
import com.campus.trade.campustradebackend.entity.Item;
import com.campus.trade.campustradebackend.entity.Transaction;
import com.campus.trade.campustradebackend.repository.UserRepository;
import com.campus.trade.campustradebackend.repository.ItemRepository;
import com.campus.trade.campustradebackend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<User> getUserByPhone(@PathVariable String phone) {
        return userRepository.findByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<User> getUserByStudentId(@PathVariable String studentId) {
        return userRepository.findByStudentId(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setPassword(userDetails.getPassword());
                    user.setPhone(userDetails.getPhone());
                    user.setEmail(userDetails.getEmail());
                    user.setAvatar(userDetails.getAvatar());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 注册新用户
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String identifier = credentials.get("identifier");
        String password = credentials.get("password");

        User user = userRepository.findByUsername(identifier)
                .orElseGet(() -> userRepository.findByPhone(identifier)
                        .orElseGet(() -> userRepository.findByStudentId(identifier)
                                .orElse(null)));

        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        return ResponseEntity.ok("Login successful");
    }

    // 获取用户相关的所有物品信息
    @GetMapping("/{userId}/items")
    public ResponseEntity<?> getUserItems(@PathVariable Long userId) {
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 查询用户出售的所有物品
        List<Item> soldItems = itemRepository.findByUserId(userId);

        // 查询与用户相关的交易记录
        List<Transaction> transactions = transactionRepository.findByBuyerIdOrSellerId(userId, userId);

        // 整理数据
        Map<String, Object> response = new HashMap<>();
        response.put("soldItems", soldItems); // 用户出售的物品
        response.put("transactions", transactions); // 用户参与的交易

        return ResponseEntity.ok(response);
    }
}