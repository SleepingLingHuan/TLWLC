package com.campus.trade.campustradebackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item; // 被交易的物品

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer; // 买家



    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // 卖家

    @ManyToOne
    @JoinColumn(name = "exchange_item_id") // 用户用于交换的物品
    private Item exchangeItem;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // 交易类型（购买或交换）

    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // 交易状态（待确认、已确认、已拒绝）

    private LocalDateTime createdAt; // 创建时间

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Item getExchangeItem() {
        return exchangeItem;
    }

    public void setExchangeItem(Item exchangeItem) {
        this.exchangeItem = exchangeItem;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}