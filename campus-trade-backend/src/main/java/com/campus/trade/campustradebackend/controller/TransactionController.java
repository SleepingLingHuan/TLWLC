package com.campus.trade.campustradebackend.controller;

import com.campus.trade.campustradebackend.entity.*;
import com.campus.trade.campustradebackend.repository.ItemRepository;
import com.campus.trade.campustradebackend.repository.TransactionRepository;
import com.campus.trade.campustradebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    // 提交购买申请
    @PostMapping("/purchase")
    @Transactional
    public ResponseEntity<?> purchaseItem(@RequestBody PurchaseRequest request) {
        // 查找物品和用户
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        User seller = item.getUser();

        // 检查物品状态
        if (!item.getStatus().equals(ItemStatus.AVAILABLE)) {
            return ResponseEntity.badRequest().body("Item is not available for purchase");
        }

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setItem(item);
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setType(TransactionType.PURCHASE);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());

        // 更新物品状态为已预订
        item.setStatus(ItemStatus.RESERVED);
        itemRepository.save(item);

        // 保存交易记录
        transactionRepository.save(transaction);

        return ResponseEntity.ok("Purchase request submitted successfully");
    }

    // 提交交换申请
    @PostMapping("/exchange")
    @Transactional
    public ResponseEntity<?> exchangeItem(@RequestBody ExchangeRequest request) {
        // 查找目标物品
        Item targetItem = itemRepository.findById(request.getTargetItemId())
                .orElseThrow(() -> new RuntimeException("Target item not found"));

        // 查找用户用于交换的物品
        Item exchangeItem = itemRepository.findById(request.getExchangeItemId())
                .orElseThrow(() -> new RuntimeException("Exchange item not found"));

        // 查找用户
        User buyer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User seller = targetItem.getUser();

        // 检查目标物品和交换物品的状态是否为 AVAILABLE
        if (!targetItem.getStatus().equals(ItemStatus.AVAILABLE)) {
            return ResponseEntity.badRequest().body("Target item is not available for exchange");
        }
        if (!exchangeItem.getStatus().equals(ItemStatus.AVAILABLE)) {
            return ResponseEntity.badRequest().body("Exchange item is not available for exchange");
        }

        // 检查用户是否拥有用于交换的物品
        if (!exchangeItem.getUser().getId().equals(buyer.getId())) {
            return ResponseEntity.badRequest().body("User does not own the exchange item");
        }

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setItem(targetItem);
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setExchangeItem(exchangeItem);
        transaction.setType(TransactionType.EXCHANGE);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());

        // 更新两个物品的状态为 RESERVED
        targetItem.setStatus(ItemStatus.RESERVED);
        exchangeItem.setStatus(ItemStatus.RESERVED);

        // 保存物品和交易记录
        itemRepository.save(targetItem);
        itemRepository.save(exchangeItem);
        transactionRepository.save(transaction);

        return ResponseEntity.ok("Exchange request submitted successfully");
    }

    // 确认交易
    @PutMapping("/{transactionId}/confirm")
    @Transactional
    public ResponseEntity<?> confirmTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 检查交易状态
        if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Transaction cannot be confirmed");
        }

        // 更新交易状态为已确认
        transaction.setStatus(TransactionStatus.CONFIRMED);

        // 更新物品状态
        if (transaction.getType() == TransactionType.PURCHASE) {
            // 购买交易：仅更新目标物品状态
            Item item = transaction.getItem();
            item.setStatus(ItemStatus.SOLD);
            itemRepository.save(item);
        } else if (transaction.getType() == TransactionType.EXCHANGE) {
            // 交换交易：更新两个物品状态
            Item targetItem = transaction.getItem();
            Item exchangeItem = transaction.getExchangeItem();

            targetItem.setStatus(ItemStatus.SOLD);
            exchangeItem.setStatus(ItemStatus.SOLD);

            itemRepository.save(targetItem);
            itemRepository.save(exchangeItem);
        }

        // 保存交易记录
        transactionRepository.save(transaction);

        return ResponseEntity.ok("Transaction confirmed successfully");
    }

    // 拒绝交易
    @PutMapping("/{transactionId}/reject")
    @Transactional
    public ResponseEntity<?> rejectTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 检查交易状态
        if (!transaction.getStatus().equals(TransactionStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Transaction cannot be rejected");
        }

        // 更新交易状态为已拒绝
        transaction.setStatus(TransactionStatus.REJECTED);

        // 更新物品状态
        if (transaction.getType() == TransactionType.PURCHASE) {
            // 购买交易：仅恢复目标物品状态
            Item item = transaction.getItem();
            item.setStatus(ItemStatus.AVAILABLE);
            itemRepository.save(item);
        } else if (transaction.getType() == TransactionType.EXCHANGE) {
            // 交换交易：恢复两个物品状态
            Item targetItem = transaction.getItem();
            Item exchangeItem = transaction.getExchangeItem();

            targetItem.setStatus(ItemStatus.AVAILABLE);
            exchangeItem.setStatus(ItemStatus.AVAILABLE);

            itemRepository.save(targetItem);
            itemRepository.save(exchangeItem);
        }

        // 保存交易记录
        transactionRepository.save(transaction);

        return ResponseEntity.ok("Transaction rejected successfully");
    }
}

// 购买请求 DTO
class PurchaseRequest {
    private Long itemId; // 物品 ID
    private Long buyerId; // 买家 ID

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }
}

// 交换请求 DTO
class ExchangeRequest {
    private Long userId; // 发起交换的用户 ID
    private Long targetItemId; // 目标物品 ID
    private Long exchangeItemId; // 用户用于交换的物品 ID

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetItemId() {
        return targetItemId;
    }

    public void setTargetItemId(Long targetItemId) {
        this.targetItemId = targetItemId;
    }

    public Long getExchangeItemId() {
        return exchangeItemId;
    }

    public void setExchangeItemId(Long exchangeItemId) {
        this.exchangeItemId = exchangeItemId;
    }
}