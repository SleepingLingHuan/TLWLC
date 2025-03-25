package com.campus.trade.campustradebackend.controller;

import java.util.List;
import com.campus.trade.campustradebackend.entity.Item;
import com.campus.trade.campustradebackend.entity.ItemStatus;
import com.campus.trade.campustradebackend.entity.User;
import com.campus.trade.campustradebackend.repository.ItemRepository;
import com.campus.trade.campustradebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.campus.trade.campustradebackend.entity.Item;
import com.campus.trade.campustradebackend.repository.ItemRepository;
import com.campus.trade.campustradebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    // 上传物品
    @PostMapping("/upload")
    public ResponseEntity<?> uploadItem(@RequestParam("userId") Long userId,
                                        @RequestParam("name") String name,
                                        @RequestParam("description") String description,
                                        @RequestParam("category") String category,
                                        @RequestParam("price") Double price,
                                        @RequestParam("campus") String campus,
                                        @RequestParam(value = "files", required = false) MultipartFile[] files) {

        // 查找用户
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 创建物品
        Item item = new Item();
        item.setUser(user);
        item.setName(name);
        item.setDescription(description);
        item.setCategory(category);
        item.setPrice(price);
        item.setCampus(campus);
        item.setStatus(ItemStatus.AVAILABLE);
        item.setCreatedAt(java.time.LocalDateTime.now());

        if (files != null && files.length > 0) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                try {
                    // 生成唯一的文件名
                    String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                    // 获取项目根目录下的 uploads 文件夹路径
                    Path uploadPath = Paths.get("uploads");
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    // 保存文件
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath);

                    // 构建图片 URL
                    String imageUrl = "/uploads/" + fileName;
                    imageUrls.add(imageUrl);
                } catch (IOException e) {
                    return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
                }
            }
            // 将图片 URL 列表保存到数据库
            item.setImageUrls(String.join(",", imageUrls));
        }

        // 保存物品
        itemRepository.save(item);

        return ResponseEntity.ok("Item uploaded successfully");
    }

    //更新物品信息
    @PutMapping("/{itemId}")
    @Transactional
    public ResponseEntity<?> updateItem(@PathVariable Long itemId, @RequestBody ItemRequest request) {
        // 查找物品
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 检查物品状态是否为 AVAILABLE
        if (!item.getStatus().equals(ItemStatus.AVAILABLE)) {
            return ResponseEntity.badRequest().body("Only AVAILABLE items can be updated");
        }

        // 更新物品信息
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        item.setCampus(request.getCampus());

        // 处理图片 URL
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            String imageUrls = String.join(",", request.getImageUrls());
            item.setImageUrls(imageUrls);
        }

        // 保存物品
        itemRepository.save(item);

        return ResponseEntity.ok("Item updated successfully");
    }

    // 获取指定物品的详细信息
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable Long itemId) {
        // 查找物品
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 返回物品信息
        return ResponseEntity.ok(item);
    }
}

// 请求 DTO
class ItemRequest {
    private Long userId; // 用户 ID
    private String name; // 物品名称
    private String description; // 物品描述
    private String category; // 物品类别
    private Double price; // 物品价格
    private String campus; // 校区信息
    private List<String> imageUrls; // 图片 URL 列表

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Getters 和 Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }
}